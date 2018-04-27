package cz.xtf.radanalytics.driver.deployment;

import cz.xtf.radanalytics.util.TestHelper;
import cz.xtf.radanalytics.waiters.SparkWaiters;
import cz.xtf.TestConfiguration;
import cz.xtf.openshift.OpenShiftUtil;
import cz.xtf.openshift.OpenShiftUtils;
import cz.xtf.openshift.logs.LogCheckerUtils;
import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.api.model.Service;
import io.fabric8.openshift.api.model.Build;
import io.fabric8.openshift.api.model.BuildConfig;
import io.fabric8.openshift.api.model.DeploymentConfig;
import io.fabric8.openshift.api.model.ImageStream;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.concurrent.TimeoutException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.lang.StrictMath.toIntExact;

@Slf4j
public class Driver {
	private static final Pattern PATTERN_LINE_WITH_CLUSTER_NAME =
			Pattern.compile("Waiting for spark master http://([a-z0-9](?:[-a-z0-9]*[a-z0-9])?(?:\\.[a-z0-9](?:[-a-z0-9]*[a-z0-9])?)*)-ui:.*");

	private final DeploymentConfig deploymentConfig;
	private final BuildConfig buildConfig;
	private final String buildConfigName;
	private final String appName;
	private final ImageStream imageStream;
	private final Service service;

	private final boolean fromDriverBuildDefinition;

	private OpenShiftUtil openshift = OpenShiftUtils.master();
	private OpenShiftUtil openshiftBuildsProject;

	protected Driver(BuildConfig buildConfig, DeploymentConfig deploymentConfig, ImageStream imageStream, Service service) {
		this(buildConfig, deploymentConfig, imageStream, service, false);
	}

	protected Driver(BuildConfig buildConfig, DeploymentConfig deploymentConfig, ImageStream imageStream, Service service, boolean fromDriverBuildDefinition) {
		this.buildConfig = buildConfig;
		this.deploymentConfig = deploymentConfig;
		this.imageStream = imageStream;
		this.service = service;
		this.buildConfigName = buildConfig.getMetadata().getName();
		this.appName = deploymentConfig.getMetadata().getName();

		this.fromDriverBuildDefinition = fromDriverBuildDefinition;
		if (fromDriverBuildDefinition) {
			log.debug("Start build from TestHelper.class");
			openshiftBuildsProject = TestHelper.openshiftBuildsProject();
		}
	}

	private Build getCurrentBuild() {
		try {
			if (fromDriverBuildDefinition) {
				log.debug("Wait on latest build in namespace: '{}'", TestConfiguration.buildNamespace());
				openshiftBuildsProject.waiters().isLatestBuildPresent(buildConfigName).execute();
				return openshiftBuildsProject.getLatestBuild(buildConfigName);
			} else {
				log.debug("Wait on latest build in namespace: default-namespace");
				openshift.waiters().isLatestBuildPresent(buildConfigName).execute();
				return openshift.getLatestBuild(buildConfigName);
			}
		} catch (TimeoutException e) {
			log.error("Timeout expired while waiting for the current Driver build to be present", e.getMessage());
			throw new IllegalStateException("Timeout expired while waiting for the current Driver build to be present", e);
		}
	}

	public DeploymentConfig getDeploymentConfig() {
		log.debug("Get deployment config was called.");
		return openshift.getDeploymentConfig(appName);
	}

	public Driver buildAndDeploy() {
		log.debug("Call build and deploy.");
		if (!fromDriverBuildDefinition) {
			log.debug("Start build from Driver.class.");
			openshift.createImageStream(imageStream);
			openshift.createBuildConfig(buildConfig);
		} else {
			log.debug("Wait on build completed.");
			waitForCurrentBuildCompletion();
		}

		openshift.createService(service);
		openshift.createDeploymentConfig(deploymentConfig);
		log.debug("Build and deploy completed.");
		return this;
	}

	public Driver startNewBuild() {
		if (fromDriverBuildDefinition) {
			log.debug("Start build in namespace: {}.", TestConfiguration.buildNamespace());
			openshiftBuildsProject.startBuild(buildConfigName);
		} else {
			log.debug("Start build from Driver.class in default namespace.");
			openshift.startBuild(buildConfigName);
		}
		return this;
	}

	public Pod getPod() {
		return openshift.getPods(appName, toIntExact(getDeploymentConfig().getStatus().getLatestVersion())).get(0);
		//return openshift.getLabeledPods("app", appName).get(0);
	}

	public String getClusterNameFromLog() {
		String[] found;
		String clusterName = null;

		waitForPatternInLogs(PATTERN_LINE_WITH_CLUSTER_NAME);

		try {
			found = LogCheckerUtils.getLinesWithFoundPatternsInLogs(
					this.getPod(), PATTERN_LINE_WITH_CLUSTER_NAME);
		} catch (IOException e) {
			log.error("Wasn't able to check Driver logs.", e.getMessage());
			throw new IllegalStateException("Wasn't able to check Driver logs.", e);
		}
		if (found != null && found.length >= 1) {
			Matcher matcher = PATTERN_LINE_WITH_CLUSTER_NAME.matcher(found[0]);
			if (matcher.matches()) {
				clusterName = matcher.group(1);
			}
		}
		return clusterName;
	}

	public String getClusterNameFromLabel() {
		return openshift.getDeploymentConfigEnvVars(appName).get("OSHINKO_CLUSTER_NAME");
	}

	public Driver waitForBuildAndDeployment() {
		log.info("Start waiting for build and deployment");
		log.debug("Call wait for current build will be completed.");
		waitForCurrentBuildCompletion();
		log.debug("Call wait for deployment of driver will be completed.");
		waitForDriverDeployed();
		log.info("Build and deploy was completed successfully.");
		return this;
	}

	public Driver waitForCurrentBuildCompletion() {
		log.debug("Start waiting for build completion.");
		try {
			if (fromDriverBuildDefinition) {
				log.debug("Waiting to prepare build. Namespace : '{}'.", TestConfiguration.buildNamespace());
				openshiftBuildsProject.waiters().hasBuildCompleted(getCurrentBuild().getMetadata().getName()).execute();
			} else {
				log.debug("Waiting to prepare build in default namespace.", TestConfiguration.masterNamespace());
				openshift.waiters().hasBuildCompleted(getCurrentBuild().getMetadata().getName()).execute();
			}
		} catch (TimeoutException e) {
			log.error("Timeout expired while waiting for Driver build to be built", e.getMessage());
			throw new IllegalStateException("Timeout expired while waiting for Driver build to be built", e);
		}

		return this;
	}

	public Driver waitForDriverDeployed() {
		try {
			// sometimes we ask for deploymentConfig version to early and we don't want to wait for version 0
			int version = toIntExact(getDeploymentConfig().getStatus().getLatestVersion());
			if (version == 0) {
				version = 1;
			}
			openshift.waiters().isDeploymentReady(appName, version).execute();
		} catch (TimeoutException e) {
			log.error("Timeout expired while waiting for Driver pod to be ready", e.getMessage());
			throw new IllegalStateException("Timeout expired while waiting for Driver pod to be ready", e);
		}
		log.info("Deployment completed.");
		return this;
	}

	public void waitForJobFinished() {
		//TODO log:(DAGScheduler: Job 0 finished) ??
	}

	public void waitForPatternInLogs(Pattern patternInLogs) {
		try {
			log.info("Start searching for pattern: {} - in pod log. Waiter was called.", patternInLogs);
			SparkWaiters.waitForPatternFound(getPod(), patternInLogs).execute();
		} catch (TimeoutException e) {
			log.error("Timeout expired while waiting for Driver Application to be finished", e.getMessage());
			throw new IllegalStateException("Timeout expired while waiting for Driver Application to be finished", e);
		}
	}

	public void waitForPodsIsReady(int countPods, String keyLabel, String valueLabel) {
		try {
			log.debug("Waiting for Pods \"{}\" to be in \"Ready\" status", valueLabel);
			openshift.waiters().areExactlyNPodsReady(countPods, keyLabel, valueLabel).execute();
		} catch (TimeoutException e) {
			log.error("Timeout exception during waiting for Pods to be in \"Ready\" status. Exception: {}", e.getMessage());
			throw new IllegalStateException("Timeout expired while waiting for Pods to be in \"Ready\" status", e);
		}
	}

	public Driver waitForPodsIsRunning(int countPods, String keyLabel, String valueLabel) {
		try {
			log.debug("Waiting for Pods \"{}\" to be in \"running phase\"", valueLabel);
			openshift.waiters().areExactlyNPodsRunning(countPods, keyLabel, valueLabel).execute();
		} catch (TimeoutException e) {
			log.error("Timeout exception during waiting for Pods to be in \"running phase\". Exception: {}", e.getMessage());
			throw new IllegalStateException("Timeout expired while waiting for Pods to be in \"running phase\"", e);
		}
		return this;
	}
}
