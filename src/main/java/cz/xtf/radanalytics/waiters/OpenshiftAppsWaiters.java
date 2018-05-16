package cz.xtf.radanalytics.waiters;

import static java.lang.StrictMath.toIntExact;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import cz.xtf.openshift.OpenShiftUtil;
import cz.xtf.openshift.OpenShiftUtils;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class OpenshiftAppsWaiters {

	private static OpenShiftUtil openshift = OpenShiftUtils.master();

	public static void waitForAppBuildAndDeployment(String appName) {
		waitForAppBuild(appName);
		waitForAppDeployment(appName);
	}

	public static void waitForAppBuild(String appName) {

		String buildName = "<not_intialized_build_for_" + appName + ">";
		try {
			openshift.waiters().isLatestBuildPresent(appName).execute();
			buildName = openshift.getLatestBuild(appName).getMetadata().getName();
			openshift.waiters().hasBuildCompleted(buildName).timeout(TimeUnit.MINUTES, 15).execute();
		} catch (TimeoutException e) {
			log.error("Timeout expired while waiting for " + buildName + " build to be built", e.getMessage());
			throw new IllegalStateException("Timeout expired while waiting for " + buildName + " build to be built", e);
		}
	}

	public static void waitForAppDeployment(String appName) {

		try {
			// sometimes we ask for deploymentConfig version to early and we don't want to wait for version 0
			int version = toIntExact(openshift.getDeploymentConfig(appName).getStatus().getLatestVersion());
			if (version == 0) {
				version = 1;
			}
			openshift.waiters().isDeploymentReady(appName, version).timeout(TimeUnit.MINUTES, 10L).execute();
		} catch (TimeoutException e) {
			log.error("Timeout expired while waiting for Deployment " + appName + " be ready", e.getMessage());
			throw new IllegalStateException("Timeout expired while waiting for " + appName + " to be ready", e);
		}
	}
}
