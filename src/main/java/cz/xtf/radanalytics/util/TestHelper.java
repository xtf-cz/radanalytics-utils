package cz.xtf.radanalytics.util;

import cz.xtf.TestConfiguration;
import cz.xtf.git.GitLabUtil;
import cz.xtf.git.GitProject;
import cz.xtf.io.IOUtils;
import cz.xtf.openshift.OpenShiftUtil;
import cz.xtf.openshift.OpenShiftUtils;
import cz.xtf.radanalytics.driver.deployment.Driver;
import cz.xtf.radanalytics.oshinko.deployment.Oshinko;
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

	public static String downloadAndGetResources(String localWorkDir, String templateFileName, String resourcesUrl) {
		log.info("Downloading and getting Resources from {}", resourcesUrl);
		String resources = null;

		try {
			if (!getResourceStatusCode(resourcesUrl)) {
				log.error("Resource is not available");
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
			log.error(e.getMessage());
		}

		return resources;
	}

	public static Boolean getResourceStatusCode(String url) {
		Integer status = 404;
		try {
			HttpClient client = HttpClientBuilder.create().build();
			HttpGet request = new HttpGet(url);
			status = client.execute(request).getStatusLine().getStatusCode();
		} catch (Exception e) {
			log.error(e.getMessage());
		}
		return status == 200;
	}
}
