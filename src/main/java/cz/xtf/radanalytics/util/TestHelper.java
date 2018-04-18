package cz.xtf.radanalytics.util;

import cz.xtf.io.IOUtils;
import cz.xtf.radanalytics.driver.deployment.Driver;
import cz.xtf.radanalytics.oshinko.deployment.Oshinko;
import cz.xtf.TestConfiguration;
import cz.xtf.git.GitLabUtil;
import cz.xtf.git.GitProject;
import cz.xtf.openshift.OpenShiftUtil;
import cz.xtf.openshift.OpenShiftUtils;
import cz.xtf.wait.SupplierWaiter;
import cz.xtf.wait.Waiter;
import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.openshift.api.model.Build;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClientBuilder;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
public class TestHelper {

	private static OpenShiftUtil openshiftBuildsProject;
	private static GitLabUtil GITLAB;

	public static synchronized GitLabUtil gitlab() {
		if (GITLAB == null) {
			GITLAB = new GitLabUtil();
		}

		return GITLAB;
	}

	public static synchronized OpenShiftUtil openshiftBuildsProject() {
		if (openshiftBuildsProject == null) {
			openshiftBuildsProject = OpenShiftUtils.master(TestConfiguration.buildNamespace());

			log.info("Cleaning up builds project: " + TestConfiguration.buildNamespace());
			openshiftBuildsProject.clean();
			openshiftBuildsProject.addRoleToGroup("system:image-puller", "system:authenticated");
		}

		return openshiftBuildsProject;
	}

	// TODO rollback?
	public static void deployAndUpdateSources(GitProject project, Driver driver, String clusterName, String fileToModify, String originalReplacePattern, String newReplacePattern) throws IOException {
		log.info("Deploy and update sources. TestHelper called");
		Path pathToProject = project.getPath();
		project.commit("Initial source code");
		project.push();
		log.info("Initial source code pushed to repo");

		driver.buildAndDeploy().waitForBuildAndDeployment();
		log.info("Application build triggered");
		Oshinko.verifySparkClustersState(clusterName, 1);

		driver.waitForPatternInLogs(Pattern.compile(originalReplacePattern));
		log.info("Original application is working successfully, let's modify it's sources");

		Path pathToFileToModify;
		List<Path> matchedFilesList = Files.find(pathToProject, 50, (path, attr) -> path.getFileName().toString().equals(fileToModify)).collect(Collectors.toList());

		if (matchedFilesList.size() == 1) {
			pathToFileToModify = matchedFilesList.get(0);
		} else {
			throw new IllegalStateException("Exactly one file named '" + fileToModify + "' should be found, but we've found " + matchedFilesList.size() + " files");
		}

		try (Stream<String> lines = Files.lines(pathToFileToModify)) {
			List<String> replaced = lines
					.map(line -> line.replaceAll(originalReplacePattern, newReplacePattern))
					.collect(Collectors.toList());
			Files.write(pathToFileToModify, replaced);
		}

		project.addAll();
		project.commit("Change line from '" + originalReplacePattern + "' to '" + newReplacePattern + "'");
		project.push();
		log.info("Modified source code pushed to repo");

		driver.startNewBuild();
		log.info("Modified application build triggered");
		driver.waitForCurrentBuildCompletion();
		log.info("Modified application build completed");

		driver.waitForDriverDeployed();
		log.info("Modified application deployed");

		driver.waitForPatternInLogs(Pattern.compile(newReplacePattern));
		log.info("Modified application is working successfully");
	}

	////////// =================
	// FIXME  TEMPORARY METHODS: remove when this code is present in XTF utilities

	// cz.xtf.openshift.OpenShiftWaiters

	/**
	 * Creates waiter for latest build presence with preconfigured timeout 5 minutes
	 * 5 seconds interval check and both logging points.
	 *
	 * @param buildConfigName name of buildConfig for which build to be waited upon
	 * @return Waiter instance
	 */
	public static Waiter isLatestBuildPresent(String buildConfigName) {
		return isLatestBuildPresent(buildConfigName, null);
	}

	/**
	 * Creates waiter for latest build presence with preconfigured timeout 5 minutes
	 * 5 seconds interval check and both logging points.
	 *
	 * @param buildConfigName name of buildConfig for which build to be waited upon
	 * @param buildNamespace  namespace containing the buildConfig
	 * @return Waiter instance
	 */
	public static Waiter isLatestBuildPresent(String buildConfigName, String buildNamespace) {
		Supplier<Build> supplier;

		if (buildNamespace == null || buildNamespace.isEmpty()) {
			supplier = () -> OpenShiftUtils.master().getLatestBuild(buildConfigName);
		} else {
			supplier = () -> OpenShiftUtils.master(buildNamespace).getLatestBuild(buildConfigName);
		}
		String reason = "Waiting for presence of latest build of buildconfig " + buildConfigName;

		//return new SupplierWaiter<Build>(supplier, build -> build != null, TimeUnit.MINUTES, 5, reason).logPoint(Waiter.LogPoint.BOTH).interval(5_000);
		return new SupplierWaiter<Build>(supplier, build -> build != null, reason).logPoint(Waiter.LogPoint.BOTH).interval(5_000);
	}

	/**
	 * Creates a waiter that waits until there aren't any pods in project.
	 * Defaults to 3 minutes timeout.
	 *
	 * @param key   label key for pod filtering
	 * @param value label value for pod filtering
	 * @return Waiter instance
	 */
	public static Waiter areNoPodsPresent(String key, String value) {
		Supplier<List<Pod>> ps = () -> OpenShiftUtils.master().getLabeledPods(key, value);
		String reason = "Waiting for no present pods with label " + key + "=" + value + ".";

		return areNoPodsPresent(ps).reason(reason);
	}

	private static Waiter areNoPodsPresent(Supplier<List<Pod>> podSupplier) {
		return new SupplierWaiter<>(podSupplier, areNoPodsPresent(), TimeUnit.MINUTES, 3);
	}

	// cz.xtf.openshift.ResourceFunctions
	public static Function<List<Pod>, Boolean> areNoPodsPresent() {
		return pods -> pods.stream().count() == 0L;
	}
	////////// =================

	public static String downloadAndGetResources(String localWorkDir, String templateFileName, String resourcesUrl) {
		log.info("Downloading and getting Resources from {}", resourcesUrl);
		String resources = null;

		try {
			if (!getResourceStatusCode(resourcesUrl)) {
				throw new Exception("Resource is not available");
			}

			log.debug("Trying to download resources and create yaml/json file");
			File WORKDIR = IOUtils.TMP_DIRECTORY.resolve(localWorkDir).toFile();

			if (WORKDIR.exists()) {
				FileUtils.deleteDirectory(WORKDIR);
			}
			if (!WORKDIR.mkdirs()) {
				throw new IOException("Cannot mkdirs " + WORKDIR);
			}

			File resourcesFile = new File(WORKDIR, templateFileName);

			URL requestUrl = new URL(resourcesUrl);
			FileUtils.copyURLToFile(requestUrl, resourcesFile, 20_000, 300_000);

			resources = resourcesFile.getPath();
		} catch (IOException e) {
			log.error("Was not able to download resources definition from {}. Exception: {}", resourcesUrl, e.getMessage());
			throw new IllegalStateException("Was not able to download resources definition from " + resourcesUrl, e);
		} catch (Exception e) {
			e.printStackTrace();
		}

		return resources;
	}

	private static Boolean getResourceStatusCode(String url) {
		Integer status = 404;
		try {
			HttpClient client = HttpClientBuilder.create().build();
			HttpGet request = new HttpGet(url);
			status = client.execute(request).getStatusLine().getStatusCode();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return status == 200;
	}
}
