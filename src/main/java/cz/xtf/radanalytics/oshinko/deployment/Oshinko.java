package cz.xtf.radanalytics.oshinko.deployment;

import static cz.xtf.radanalytics.util.TestHelper.downloadAndGetResources;

import org.assertj.core.api.Assertions;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;

import cz.xtf.openshift.OpenShiftUtil;
import cz.xtf.openshift.OpenShiftUtils;
import cz.xtf.openshift.PodService;
import cz.xtf.radanalytics.configuration.RadanalyticsConfiguration;
import cz.xtf.radanalytics.openshift.openshift.web.OpenshiftWebUI;
import cz.xtf.radanalytics.oshinko.cli.OshinkoCli;
import cz.xtf.radanalytics.oshinko.web.OshinkoPoddedWebUI;
import cz.xtf.radanalytics.waiters.OpenshiftAppsWaiters;
import cz.xtf.radanalytics.waiters.SparkWaiters;
import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.api.model.PodBuilder;
import io.fabric8.kubernetes.api.model.ServiceAccount;
import io.fabric8.kubernetes.api.model.ServiceAccountBuilder;
import io.fabric8.openshift.api.model.RouteSpec;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

/**
 * This class is taking care of Oshinko components deployment on OpenShift
 */
@Slf4j
public class Oshinko {
	private static final OpenShiftUtil openshift = OpenShiftUtils.master();
	private static final String OSHINKO_WEBUI_REFRESH_INTERVAL = "10";  //Specifying interval for refreshing UI on Cluster page in sec.
	private static String OSHINKO_WEBUI_RESOURCES;

	private static String OSHINKO_ALL_RESOURCES;

	@Getter
	private static final String defaultServiceAccountName = "oshinko";

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
	 * Installs all Oshinko resources, usually specified in resources.yaml on radanalytics.io website
	 * oshinko-webui templates, S2I templates, etc.
	 */
	public static void installOshinkoResources() {

		log.info("Installing Oshinko resources");

		if (OSHINKO_ALL_RESOURCES == null) {
			OSHINKO_ALL_RESOURCES = downloadAndGetResources("radanalyticsio", "oshinko-all-resources.yaml", RadanalyticsConfiguration.templateOshinkoAllResourcesUrl());
		}

		try (InputStream is = Files.newInputStream(Paths.get(OSHINKO_ALL_RESOURCES))) {
			log.debug("Load Oshinko resources");
			openshift.loadResource(is);
		} catch (IOException e) {
			log.error("Exception during installing of Oshinko resources: {}", e.getMessage());
			throw new IllegalStateException("Wasn't able to install Oshinko resource");
		}

		log.info("Oshinko resources installed");
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
		Map<String, String> mapParams = new HashMap<>();
		mapParams.put("OSHINKO_REFRESH_INTERVAL", OSHINKO_WEBUI_REFRESH_INTERVAL);

		if (OSHINKO_WEBUI_RESOURCES == null) {
			OSHINKO_WEBUI_RESOURCES = downloadAndGetResources("radanalyticsio", oshinkoWebUITemplate, RadanalyticsConfiguration.templateOshinkoWebUiResourcesUrl());
		}
		try (InputStream is = Files.newInputStream(Paths.get(OSHINKO_WEBUI_RESOURCES))) {
			log.debug("Load Oshinko WebUI template");
			openshift.loadResource(is);
		} catch (IOException e) {
			log.error("Exception during loading of Oshinko WebUI template: {}", e.getMessage());
			throw new IllegalStateException("Wasn't able to load Oshinko WebUI template");
		}

		log.debug("Process template with name \"{}\"", templateName);
		openshift.processAndDeployTemplate(templateName, mapParams);
		log.debug("Creating route \"{}\"", routeName);
		RouteSpec routeSpec = openshift.getRoute(routeName).getSpec();

		OpenshiftAppsWaiters.waitForAppDeployment(routeName);

		return OshinkoPoddedWebUI.getInstance("http://" + routeSpec.getHost() + routeSpec.getPath());
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
				.addNewContainer().withName(appCLI).withImage(RadanalyticsConfiguration.imageOshinkoCli()).endContainer()
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
			openshift.waiters().areExactlyNPodsReady(workersCount + mastersCount, clusterNameKey, clusterName).timeout(TimeUnit.MINUTES, 10).execute();
		} catch (TimeoutException e) {
			log.error("Timeout exception during creating Spark Cluster \"{}\". Exception: {}", clusterName, e.getMessage());
			throw new IllegalStateException("Timeout expired while waiting for Spark cluster to be ready", e);
		}

		try {
			log.debug("Waiting for Spark Cluster Pod is ready");
			openshift.waiters().areExactlyNPodsRunning(workersCount + mastersCount, clusterNameKey, clusterName).timeout(TimeUnit.MINUTES, 10).execute();
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
			log.debug("Verifying there are not Pods present");
			openshift.waiters().areNoPodsPresent("oshinko-cluster", clusterName).execute();
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

	/**
	 * Will deploy webUI secure pod for Oshinko and waits till ready to handle requests.
	 *
	 * @return Configured UI api to work with deployed webUI
	 */
	public static OpenshiftWebUI deployWebUISecurePod() {
		String templateName = "oshinko-webui-secure";
		String routeName = "oshinko-web-oaproxy";
		String appName = "oshinko-web";
		String oshinkoWebUISecureTemplate = "oshinko-webui-secure-template.yaml";
		return deployWebUISecurePodCommonLogic(templateName, routeName, oshinkoWebUISecureTemplate, appName);
	}

	public static OpenshiftWebUI deployWebUISecurePod(String templateName, String routeName, String oshinkoWebUISecureTemplate, String appName) {
		return deployWebUISecurePodCommonLogic(templateName, routeName, oshinkoWebUISecureTemplate, appName);
	}

	private static OpenshiftWebUI deployWebUISecurePodCommonLogic(String templateName, String routeName, String oshinkoWebUISecureTemplate, String appName) {
		log.info("Deploying WebUI Secure Pod");
		Map<String, String> mapParams = new HashMap<>();
		mapParams.put("OSHINKO_REFRESH_INTERVAL", OSHINKO_WEBUI_REFRESH_INTERVAL);

		if (OSHINKO_WEBUI_RESOURCES == null) {
			OSHINKO_WEBUI_RESOURCES = downloadAndGetResources("radanalyticsio", oshinkoWebUISecureTemplate, RadanalyticsConfiguration.templateOshinkoAllResourcesUrl());
		}
		try (InputStream is = Files.newInputStream(Paths.get(OSHINKO_WEBUI_RESOURCES))) {
			log.debug("Load Oshinko WebUI Secure template");
			openshift.loadResource(is);
		} catch (IOException e) {
			log.error("Exception during loading of Oshinko WebUI Secure template: {}", e.getMessage());
			throw new IllegalStateException("Wasn't able to load Oshinko WebUI Secure template");
		}

		log.debug("Process template with name \"{}\"", templateName);
		openshift.processAndDeployTemplate(templateName, mapParams);
		log.debug("Creating route \"{}\"", routeName);
		RouteSpec routeSpec = openshift.getRoute(routeName).getSpec();

		OpenshiftAppsWaiters.waitForAppDeployment(appName);

		return OpenshiftWebUI.getInstance("https://" + routeSpec.getHost() + routeSpec.getPath());
	}
}
