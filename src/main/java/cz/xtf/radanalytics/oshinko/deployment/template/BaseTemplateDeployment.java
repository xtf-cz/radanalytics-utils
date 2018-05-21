package cz.xtf.radanalytics.oshinko.deployment.template;

import cz.xtf.openshift.OpenShiftUtil;
import cz.xtf.openshift.OpenShiftUtils;
import cz.xtf.radanalytics.util.TestHelper;
import cz.xtf.radanalytics.waiters.OpenshiftAppsWaiters;
import io.fabric8.openshift.api.model.Template;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Map;


@Slf4j
public class BaseTemplateDeployment {
	private static final OpenShiftUtil openshift = OpenShiftUtils.master();

	/**
	 * Folder where will be saved template file
	 */
	private static String WORK_DIR = "";

	/**
	 * Creates the Openshift application from template and specified parameters
	 * Similar way it is done via: oc new-app --template=[your template] -p [one or multiple parameters]
	 *
	 * @param templateResourcePath path or url to template file
	 * @param templateResourceFileName file name with extension like: pythonbuilddc.json
	 * @param params parameters for application instance like: -p APPLICATION_NAME=winemap etc.
	 */
	public static void deploy(String templateResourcePath,
				String templateResourceFileName,
				Map<String, String> params) {

		if (WORK_DIR.isEmpty()) {
			WORK_DIR = "template";
		}

		String templateLocation = TestHelper.downloadAndGetResources(WORK_DIR, templateResourceFileName, templateResourcePath);

		Template oshinkoSparkTemplate = null;
		try (InputStream is = Files.newInputStream(Paths.get(templateLocation))) {
			oshinkoSparkTemplate = openshift.loadAndCreateTemplate(is);
		} catch (IOException e) {
			log.error(e.getMessage());
		}
		log.info("{}- start deploying", oshinkoSparkTemplate.getMetadata().getName());
		openshift.processAndDeployTemplate(oshinkoSparkTemplate.getMetadata().getName(), params);
		OpenshiftAppsWaiters.waitForAppBuildAndDeployment(params.get("APPLICATION_NAME"));
		log.info("{}- deployed");
	}

	/**
	 * Creates the Openshift application from template and specified parameters
	 * Similar way it is done via: oc new-app --template=[your template] -p [one or multiple parameters]
	 *
	 * @param workDir Folder where will be saved template file
	 * @param templateResourcePath path or url to template file
	 * @param templateResourceFileName file name with extension like: pythonbuilddc.json
	 * @param params parameters for application instance like: -p APPLICATION_NAME=winemap etc.
	 */
	public static void deploy(String workDir, String templateResourcePath,
				String templateResourceFileName,
				Map<String, String> params) {

		WORK_DIR = workDir;
		deploy(templateResourcePath, templateResourceFileName, params);
	}
}
