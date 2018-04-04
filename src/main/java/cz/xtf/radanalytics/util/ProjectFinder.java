package cz.xtf.radanalytics.util;

import cz.xtf.io.IOUtils;
import lombok.extern.slf4j.Slf4j;

import java.nio.file.Path;
import java.nio.file.Paths;

@Slf4j
public class ProjectFinder {
	
	public static Path findApplicationDirectory(String appName) {
		return findApplicationDirectory(null, appName, null);
	}

	public static Path findApplicationDirectory(String appName, String appSubModuleName) {
		return findApplicationDirectory(null, appName, appSubModuleName);
	}

	public static Path findApplicationDirectory(String moduleName, String appName, String appSubModuleName) {
		Path basePath = moduleName == null ? Paths.get("") : IOUtils.findProjectRoot().resolve(moduleName);
		Path relativePath = appSubModuleName == null ? Paths.get(appName) : Paths.get(appSubModuleName, appName);

		Path testApp = basePath.resolve("src/test/resources/apps").resolve(relativePath);
		Path mainApp = basePath.resolve("src/main/resources/apps").resolve(relativePath);
		
		if (testApp.toFile().exists()) {
			log.info("Found project {} at the path: {}", appName, testApp.toAbsolutePath().toString());
			return testApp;
		} else if (mainApp.toFile().exists()) {
			log.info("Found project {} at the path: {}", appName, mainApp.toAbsolutePath().toString());
			return mainApp;
		}

		throw new IllegalStateException("Project app not found");
	}
}