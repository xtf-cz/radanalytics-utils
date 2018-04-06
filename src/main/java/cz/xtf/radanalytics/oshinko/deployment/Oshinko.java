package cz.xtf.radanalytics.oshinko.deployment;

import cz.xtf.radanalytics.oshinko.cli.OshinkoCli;
import cz.xtf.radanalytics.oshinko.web.OshinkoPoddedWebUI;
import cz.xtf.radanalytics.util.TestHelper;
import cz.xtf.radanalytics.util.waiters.SparkWaiters;

import cz.xtf.io.IOUtils;
import cz.xtf.openshift.OpenShiftUtil;
import cz.xtf.openshift.OpenShiftUtils;
import cz.xtf.openshift.PodService;
import cz.xtf.openshift.imagestream.ImageRegistry;
import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.api.model.PodBuilder;
import io.fabric8.kubernetes.api.model.ServiceAccount;
import io.fabric8.kubernetes.api.model.ServiceAccountBuilder;
import io.fabric8.openshift.api.model.RouteSpec;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.assertj.core.api.Assertions;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;

/**
 * This class is taking care of Oshinko components deployment on OpenShift
 */
@Slf4j
public class Oshinko {
	private static final OpenShiftUtil openshift = OpenShiftUtils.master();
	private static final String OSHINKO_WEBUI_RESOURCES_URL = "https://raw.githubusercontent.com/radanalyticsio/oshinko-webui/master/tools/ui-template.yaml";
	@Getter
	private static final String defaultServiceAccountName = "oshinko";
	private static String OSHINKO_WEBUI_RESOURCES;

	public static ServiceAccount deployServiceAccount() {
		return deployServiceAccount(getDefaultServiceAccountName());
	}

	public static ServiceAccount deployServiceAccount(String name) {
		return deployServiceAccount(name, "edit");
	}

	public static ServiceAccount deployServiceAccount(String name, String role) {
		log.info("Creating service account configuration with name: {}", name);
		ServiceAccount sa = new ServiceAccountBuilder().withNewMetadata().withName(Oshinko.getDefaultServiceAccountName()).endMetadata().build();
		log.debug("Deploying service account with api version: {}", sa.getApiVersion());
		openshift.createServiceAccount(sa);
		log.debug("Adding role \"{}\" to service account", role);
		openshift.addRoleToServiceAccount(role, sa.getMetadata().getName());

		return sa;
	}

	/**
	 * Will deploy webUI pod for Oshinko and waits till ready to handle requests.
	 *
	 * @return Configured UI api to work with deployed webUI
	 */
	public static OshinkoPoddedWebUI deployWebUIPod() {
		String templateName = "oshinko-webui";
		String routeName = "oshinko-web";
		String oshinkoWebUITemplate = "oshinko-webui-template.yaml";
		return deployWebUIPodCommonLogic(templateName, routeName, oshinkoWebUITemplate);
	}

	public static OshinkoPoddedWebUI deployWebUIPod(String templateName, String routeName, String oshinkoWebUITemplate) {
		return deployWebUIPodCommonLogic(templateName, routeName, oshinkoWebUITemplate);
	}

	private static OshinkoPoddedWebUI deployWebUIPodCommonLogic(String templateName, String routeName, String oshinkoWebUITemplate) {
		log.info("Deploying WebUI Pod");

		try (InputStream is = Files.newInputStream(Paths.get(getOshinkoWebuiResources(oshinkoWebUITemplate)))) {
			log.debug("Deleting existing pod and creating or replacing Pod");
			openshift.client().load(is).deletingExisting().createOrReplace();
		} catch (IOException e) {
			log.error("Exception during deleting or creating pod: {}", e.getMessage());
			throw new IllegalStateException("Timeout expired while waiting for Oshinko server availability");
		}

		log.debug("Creating template with name \"{}\"", templateName);
		openshift.client().lists()
				.create(openshift.client().templates().withName(templateName).process());
		log.debug("Creating route \"{}\"", routeName);
		RouteSpec route = openshift.client().routes().withName(routeName).get().getSpec();

		try {
			log.debug("Waiting for that Pod is ready");
			openshift.waiters().areExactlyNPodsReady(1, "name", routeName).execute();
		} catch (TimeoutException e) {
			log.error("Timeout exception during creating Pod: {}", e.getMessage());
			throw new IllegalStateException("Timeout expired while waiting for Oshinko server availability");
		}

		return OshinkoPoddedWebUI.getInstance(route.getHost() + route.getPath());
	}

	/**
	 * Downloads Oshinko Webui resources yaml file if it not present in local tmp directory
	 *
	 * @return path to Oshinko Webui resources yaml file
	 */
	private static String getOshinkoWebuiResources(String oshinkoWebUITemplate) {
		log.info("Getting Oshinko Web UI Resources from temp directory");

		if (OSHINKO_WEBUI_RESOURCES == null) {
			try {
				log.debug("Trying to download resources and create yaml file");
				File WORKDIR = IOUtils.TMP_DIRECTORY.resolve("radanalyticsio").toFile();

				if (WORKDIR.exists()) {
					FileUtils.deleteDirectory(WORKDIR);
				}
				if (!WORKDIR.mkdirs()) {
					throw new IOException("Cannot mkdirs " + WORKDIR);
				}

				File resourcesFile = new File(WORKDIR, oshinkoWebUITemplate);

				URL requestUrl = new URL(OSHINKO_WEBUI_RESOURCES_URL);
				FileUtils.copyURLToFile(requestUrl, resourcesFile, 20_000, 300_000);

				OSHINKO_WEBUI_RESOURCES = resourcesFile.getPath();
			} catch (IOException e) {
				log.error("Was not able to download resources definition from {}. Exception: {}", OSHINKO_WEBUI_RESOURCES_URL, e.getMessage());
				throw new IllegalStateException("Was not able to download resources definition from " + OSHINKO_WEBUI_RESOURCES_URL, e);
			}
		}

		return OSHINKO_WEBUI_RESOURCES;
	}

	/**
	 * Will deploy custom build pod (docker image: zroubalik/oshinko-cli), which contains both Openshift CLI and Oshinko CLI installed
	 * Waits till ready to handle requests
	 *
	 * @return Configured CLI based API to work with Oshinko
	 */
	public static OshinkoCli deployCliPod() {
		String appCLI = "oshinko-cli";
		return deployCliPodCommonLogic(appCLI);
	}

	public static OshinkoCli deployCliPod(String appCLI) {
		return deployCliPodCommonLogic(appCLI);
	}

	private static OshinkoCli deployCliPodCommonLogic(String appCLI) {
		log.info("Creating CLI Pod for Openshift and Oshinko");
		Pod oshinkoCliPod;
		PodService podService;

		deployServiceAccount();

		log.info("Creating Pod \"{}\" configuration", appCLI);
		oshinkoCliPod = new PodBuilder()
				.withNewMetadata().withName(appCLI).addToLabels("name", appCLI).endMetadata()
				.withNewSpec()
				.addNewContainer().withName(appCLI).withImage(ImageRegistry.get().oshinkoCli()).endContainer()
				.endSpec().build();

		log.debug("Creating Pod \"{}\"", oshinkoCliPod.getMetadata().getName());
		openshift.createPod(oshinkoCliPod);
		podService = new PodService(oshinkoCliPod);

		try {
			log.debug("Waiting for Pod \"{}\" is ready", oshinkoCliPod.getMetadata().getName());
			openshift.waiters().areExactlyNPodsReady(1, "name", appCLI).execute();
		} catch (TimeoutException e) {
			log.error("Timeout exception during creating Pod: {}", e.getMessage());
			throw new IllegalStateException("Timeout expired while waiting for Oshinko server availability");
		}

		return OshinkoCli.getInstance(podService);
	}

	public static void verifySparkClustersState(String clusterName, int workersCount) {
		log.info("Verifying state of the cluster \"{}\"", clusterName);
		verifySparkClustersState(clusterName, 1, workersCount);
	}

	public static void verifySparkClustersState(String clusterName, int mastersCount, int workersCount) {
		String clusterNameKey = "oshinko-cluster";
		String clusterTypeKey = "oshinko-type";

		try {
			log.debug("Waiting for Spark Cluster \"{}\" is ready", clusterName);
			openshift.waiters().areExactlyNPodsReady(workersCount + mastersCount, clusterNameKey, clusterName).execute();
		} catch (TimeoutException e) {
			log.error("Timeout exception during creating Spark Cluster \"{}\". Exception: {}", clusterName, e.getMessage());
			throw new IllegalStateException("Timeout expired while waiting for Spark cluster to be ready", e);
		}

		try {
			log.debug("Waiting for Spark Cluster Pod is ready");
			openshift.waiters().areExactlyNPodsRunning(workersCount + mastersCount, clusterNameKey, clusterName).execute();
		} catch (TimeoutException e) {
			log.error("Timeout exception during creating Spark Cluster Pod. Exception: {}", e.getMessage());
			throw new IllegalStateException("Timeout expired while waiting for Spark cluster pods to be in \"running phase\"", e);
		}

		List<Pod> masterPods = openshift.getLabeledPods(Collections.singletonMap(clusterTypeKey, "master"))
				.stream().filter(pod -> clusterName.equals(pod.getMetadata().getLabels().get(clusterNameKey))).collect(Collectors.toList());
		List<Pod> workerPods = openshift.getLabeledPods(Collections.singletonMap(clusterTypeKey, "worker"))
				.stream().filter(pod -> clusterName.equals(pod.getMetadata().getLabels().get(clusterNameKey))).collect(Collectors.toList());
		List<Pod> clusterPods = openshift.getLabeledPods(Collections.singletonMap(clusterNameKey, clusterName));

		Assertions.assertThat(masterPods).hasSize(mastersCount);
		Assertions.assertThat(workerPods).hasSize(workersCount);
		Assertions.assertThat(clusterPods).hasSize(workersCount + mastersCount);

		if (mastersCount > 0 && workersCount > 0) {
			List<String> listOfWorkerPodNames = new ArrayList<>();
			try {
				workerPods.forEach(pod -> listOfWorkerPodNames.add(pod.getMetadata().getName()));
				log.debug("Verifying that Spark workers \"{}\" registered with master", listOfWorkerPodNames.toString());
				SparkWaiters.areSparkWorkersRegisteredWithMaster(workerPods).execute();
			} catch (TimeoutException e) {
				log.error("Timeout exception while waiting for Spark workers to be registered with master. Exception: {}", e.getMessage());
				throw new IllegalStateException("Timeout expired while waiting for Spark workers to be registered with master", e);
			}
		}
	}

	public static void verifyNoClusterPresent(String clusterName) {
		String clusterNameKey = "oshinko-cluster";
		String clusterTypeKey = "oshinko-type";

		try {
			// FIXME replace with XTF utilities method
			log.debug("Verifying there are not Pods present");
			TestHelper.areNoPodsPresent("oshinko-cluster", clusterName).execute();
		} catch (TimeoutException e) {
			log.error("Timeout exception while waiting for Spark cluster to be destroyed. Exception: {}", e);
			throw new IllegalStateException("Timeout expired while waiting for Spark cluster to be destroyed", e);
		}

		List<Pod> masterPods = openshift.getLabeledPods(Collections.singletonMap(clusterTypeKey, "master"))
				.stream().filter(pod -> clusterName.equals(pod.getMetadata().getLabels().get(clusterNameKey))).collect(Collectors.toList());
		List<Pod> workerPods = openshift.getLabeledPods(Collections.singletonMap(clusterTypeKey, "worker"))
				.stream().filter(pod -> clusterName.equals(pod.getMetadata().getLabels().get(clusterNameKey))).collect(Collectors.toList());
		List<Pod> clusterPods = openshift.getLabeledPods(Collections.singletonMap(clusterNameKey, clusterName));

		Assertions.assertThat(masterPods).isEmpty();
		Assertions.assertThat(workerPods).isEmpty();
		Assertions.assertThat(clusterPods).isEmpty();
	}
}
