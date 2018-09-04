package cz.xtf.radanalytics.oshinko.cli;

import cz.xtf.openshift.OpenShiftUtil;
import cz.xtf.openshift.OpenShiftUtils;
import cz.xtf.openshift.PodService;
import cz.xtf.radanalytics.configuration.RadanalyticsConfiguration;
import cz.xtf.radanalytics.oshinko.api.OshinkoAPI;
import cz.xtf.radanalytics.oshinko.cli.service.SparkClusterService;
import cz.xtf.radanalytics.oshinko.entity.SparkCluster;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Oshinko CLI based implementation of Oshinko API. Performs calls to pre-deployed pod, which should contain Oshinko CLI.
 */
@Slf4j
public class OshinkoCli implements OshinkoAPI {

	private static final OpenShiftUtil openshift = OpenShiftUtils.master();

	private static final OshinkoCliOptions OSHINKO_OPTIONS = OshinkoCliOptions.builder()
			.insecureSkipTlsVerify(true)
			.token("")
			.config("/opt/kube/config")
			.build();

	private final PodService podService;
	private final String OC_CMD = "/opt/oc";
	private final String OSHINKO_CMD = "/opt/oshinko";

	public OshinkoCli(PodService podService) {
		log.debug("Create oshinko instance.");
		this.podService = podService;
		initOshinkoCli();
	}

	public static OshinkoCli getInstance(PodService podService) {
		return new OshinkoCli(podService);
	}

	private void initOshinkoCli() {
		log.debug("Login to the openshift and switched to a project. Necessary for work with oshinko.");
		ocLogin();
		ocSwitchToCurrentProject();
	}

	@Override
	public boolean createCluster(String clusterName) {
		return createCluster(clusterName, -10);
	}

	@Override
	public boolean createCluster(String clusterName, int workersCount) {
		return createCluster("", "", clusterName, workersCount, -10, null, null, null, RadanalyticsConfiguration.imageOpenshiftSpark());
	}

	@Override
	public boolean createCluster(String metrics, String exposeUi, String clusterName, int workersCount, int mastersCount, String masterConfig, String workerConfig, String storedConfig, String sparkImage) {
		log.debug("Oshinko create cluster {} --metrics={} --exposeui={} --workers={} --masters={} --masterconfig={} --storedconfig={} --image={}", clusterName, metrics, exposeUi, workerConfig, masterConfig, masterConfig, workerConfig, storedConfig, sparkImage);
		final List<String> cmd = new ArrayList<>();
		cmd.add("create");
		cmd.add(clusterName);

		if (metrics != null && metrics != "") {
			cmd.add("--metrics=" + metrics);
		}
		if (exposeUi != null && exposeUi != "") {
			cmd.add("--exposeui=" + exposeUi);
		}
		if (workersCount >= -1) {
			cmd.add("--workers=" + Integer.toString(workersCount));
		}
		if (mastersCount >= -1) {
			cmd.add("--masters=" + Integer.toString(mastersCount));
		}
		if (masterConfig != null) {
			cmd.add("--masterconfig=" + masterConfig);
		}
		if (workerConfig != null) {
			cmd.add("--workerconfig=" + workerConfig);
		}
		if (storedConfig != null) {
			cmd.add("--storedconfig=" + storedConfig);
		}
		if (sparkImage != null) {
			cmd.add("--image=" + sparkImage);
		}

		String result = performOshinkoCmd(cmd.toArray(new String[cmd.size()]));

		if (!result.startsWith("cluster \"" + clusterName + "\" created")) {
			//throw new IllegalStateException("There was a problem during the create operation of Spark cluster: " + clusterName);
			log.debug("Cluster wasn't created : ", result);
			return false;
		}
		log.debug("Cluster {} was created successfully.", clusterName);
		return true;
	}

	@Override
	public SparkCluster getCluster(String clusterName) {
		SparkCluster sparkCluster = null;
		String result = performOshinkoCmd("get", clusterName, "-o", "json");
		if (result.isEmpty()) {
			sparkCluster = null;
		} else {
			List<SparkCluster> sparkClusters = SparkClusterService.sparkClustersFromJson(result);
			sparkCluster = sparkClusters.get(0);
		}
		return sparkCluster;
	}

	@Override
	public SparkCluster getClusterConfig(String clusterName) {
		return getCluster(clusterName);
	}

	@Override
	public List<SparkCluster> listClusters() {
		String result = performOshinkoCmd("get", "-o", "json");
		log.debug("oshinko get -o json. return: {} ", result);
		return SparkClusterService.sparkClustersFromJson(result);
	}

	@Override
	public boolean scaleCluster(String clusterName, int workersCount) {
		log.debug("oshinko scale {} --workers={}", clusterName, workersCount);
		String result = performOshinkoCmd("scale", clusterName, "--workers=" + Integer.toString(workersCount));

		if (!result.startsWith("cluster \"" + clusterName + "\" scaled")) {
			log.debug(result);
			//throw new IllegalStateException("There was a problem during the scale operation of Spark cluster: " + clusterName);
			return false;
		}
		log.debug("Cluster {} was scaled to {} workers", clusterName, workersCount);
		return true;
	}

	@Override
	public boolean deleteCluster(String clusterName) {
		log.debug("oshinko delete {}", clusterName);
		String result = performOshinkoCmd("delete", clusterName);

		if (!result.startsWith("cluster \"" + clusterName + "\" deleted")) {
			log.debug(result);
			//throw new IllegalStateException("There was a problem during the delete operation of Spark cluster: " + clusterName);
			return false;
		}
		log.debug("Cluster {} was deleted", clusterName);
		return true;
	}

	private void ocLogin() {
		String username = openshift.client().getConfiguration().getUsername();
		String password = openshift.client().getConfiguration().getPassword();
		String masterUrl = openshift.client().getMasterUrl().toString();
		log.debug("oc login -u {} -p {} to {}", username, password, masterUrl);

		int retryCount = 0;
		String result = null;
		long interval = 10 * 1000L;

		while (retryCount < 10) {
			retryCount++;
			log.debug("Retry " + retryCount + " to login to the Openshift");
			result = podService.exec(OC_CMD, OSHINKO_OPTIONS.getConfig(), OSHINKO_OPTIONS.getInsecureSkipTlsVerify(), "login", masterUrl, "-u",
					username, "-p", password);
			if (result.startsWith("Login successful")) {
				break;
			} else {
				waitFor(interval);
			}
		}

		if (!result.startsWith("Login successful")) {
			log.error("There was a problem when login to Openshift. login return : {}", result);
			throw new IllegalStateException("There was a problem when login to Openshift.");
		}

		log.debug("Login successfully.");
	}

	private void ocSwitchToCurrentProject() {
		String project = openshift.client().getNamespace();
		log.debug("Switching to project: {}.", project);
		String result = podService.exec(OC_CMD, OSHINKO_OPTIONS.getConfig(), "project", "-q", project);

		if (!result.startsWith(project)) {
			log.error("There was a problem when switching to Openshift project: {}", project);
			throw new IllegalStateException("There was a problem when switching to the Openshift project: " + project);
		}
		log.debug("Switching to project: {}.", project);
	}

	private String ocGetToken() {
		String whoami = podService.exec(OC_CMD, OSHINKO_OPTIONS.getConfig(), "whoami", "-t").trim();
		log.debug("oc whoami -t {}", whoami);
		return whoami;
	}

	private String performOshinkoCmd(String... args) {
		return performOshinkoCmd(false, args);
	}

	private String performOshinkoCmd(boolean forceRequestNewOcLogin, String... args) {
		log.debug("Calling custom oshinko command, with the args: {}", Arrays.toString(args));
		boolean tokenIsAvailable = OSHINKO_OPTIONS.getToken().equals("--token=");

		if (tokenIsAvailable || forceRequestNewOcLogin) {
			log.debug("Create a new oshinko cli instance for a custom command.");
			initOshinkoCli();
			OSHINKO_OPTIONS.setToken(ocGetToken());
		}

		final List<String> cmd = new ArrayList<>();
		cmd.add(OSHINKO_CMD);
		cmd.add(OSHINKO_OPTIONS.getInsecureSkipTlsVerify());
		cmd.add(OSHINKO_OPTIONS.getConfig());
		cmd.add(OSHINKO_OPTIONS.getToken());
		cmd.addAll(Arrays.asList(args));

		String exec = podService.exec(cmd.toArray(new String[cmd.size()]));
		log.debug("Will be executed custom oshinko : {}", exec);
		return exec;
	}

	private void waitFor(long interval) {
		try {
			Thread.sleep(interval);
		} catch (InterruptedException e) {
			log.debug(e.getMessage());
		}
	}
}
