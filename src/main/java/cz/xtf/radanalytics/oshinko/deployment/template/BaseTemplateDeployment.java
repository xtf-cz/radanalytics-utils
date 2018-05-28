package cz.xtf.radanalytics.oshinko.deployment.template;

import cz.xtf.openshift.OpenShiftUtil;
import cz.xtf.openshift.OpenShiftUtils;
import cz.xtf.radanalytics.util.TestHelper;
import cz.xtf.radanalytics.waiters.OpenshiftAppsWaiters;
import io.fabric8.kubernetes.api.model.EnvVar;
import io.fabric8.kubernetes.api.model.KubernetesList;
import io.fabric8.openshift.api.model.DeploymentConfig;
import io.fabric8.openshift.api.model.Template;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


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
	 * @param templateResourcePath     path or url to template file
	 * @param templateResourceFileName file name with extension like: pythonbuilddc.json
	 * @param params                   parameters for application instance like: -p APPLICATION_NAME=winemap etc.
	 */
	public static void deploy(String templateResourcePath,
				String templateResourceFileName,
				Map<String, String> params) {

		if (WORK_DIR.isEmpty()) {
			WORK_DIR = "template";
		}

		Template oshinkoSparkTemplate = createTemplate(templateResourcePath, templateResourceFileName);

		log.info("{}- start deploying", oshinkoSparkTemplate.getMetadata().getName());
		openshift.processAndDeployTemplate(oshinkoSparkTemplate.getMetadata().getName(), params);
		OpenshiftAppsWaiters.waitForAppBuildAndDeployment(params.get("APPLICATION_NAME"));
		log.info("{}- deployed");
	}


	/**
	 * Creates the Openshift application from template and specified parameters
	 * Similar way it is done via: oc new-app --template=[your template] -p [one or multiple parameters]
	 *
	 * @param workDir                  Folder where will be saved template file
	 * @param templateResourcePath     path or url to template file
	 * @param templateResourceFileName file name with extension like: pythonbuilddc.json
	 * @param params                   parameters for application instance like: -p APPLICATION_NAME=winemap etc.
	 */
	public static void deploy(String workDir, String templateResourcePath,
				String templateResourceFileName,
				Map<String, String> params) {

		WORK_DIR = workDir;
		deploy(templateResourcePath, templateResourceFileName, params);
	}


	/**
	 * Creates the Openshift application from template and specified parameters
	 * Similar way it is done via:
	 * oc new-app --template=[your template] -p [one or multiple parameters] -e [one or multiple environment variables]
	 *
	 * @param templateResourcePath     path or url to template file
	 * @param templateResourceFileName file name with extension like: pythonbuilddc.json
	 * @param params                   parameters for application instance like: -p APPLICATION_NAME=winemap etc.
	 * @param envars                   environment variable for application instance like: -e SERVER=postgesql etc.
	 */
	public static void deploy(String templateResourcePath,
				String templateResourceFileName,
				Map<String, String> params, Map<String, String> envars) {

		Template applicationTemplate = createTemplate(templateResourcePath, templateResourceFileName);
		KubernetesList objects = openshift.processTemplate(applicationTemplate.getMetadata().getName(), params);

		DeploymentConfig deploymentConfig = getApplicationDeploymentConfig(objects);
		updateEnvVarsInContainer(envars, deploymentConfig);

		openshift.createResources(objects);
		OpenshiftAppsWaiters.waitForAppBuildAndDeployment(params.get("APPLICATION_NAME"));
	}

	private static DeploymentConfig getApplicationDeploymentConfig(KubernetesList objects) {
		return (DeploymentConfig) objects
				.getItems().stream()
				.filter(o -> o.getKind().contains("DeploymentConfig")
				).findFirst().get();
	}

	private static void updateEnvVarsInContainer(Map<String, String> envars, DeploymentConfig deploymentConfig) {
		List<EnvVar> oldVars = deploymentConfig.getSpec().getTemplate().getSpec().getContainers().get(0).getEnv();
		List<EnvVar> vars = envars.entrySet().stream()
				.map(entry -> new EnvVar(entry.getKey(), entry.getValue(), null))
				.collect(Collectors.toList());
		vars.addAll(oldVars);

		deploymentConfig.getSpec().getTemplate().getSpec().getContainers().get(0).setEnv(vars);
	}

	private static Template createTemplate(String templateResourcePath, String templateResourceFileName) {
		String templateLocation = TestHelper.downloadAndGetResources(WORK_DIR, templateResourceFileName, templateResourcePath);

		Template oshinkoSparkTemplate = null;
		try (InputStream is = Files.newInputStream(Paths.get(templateLocation))) {
			oshinkoSparkTemplate = openshift.loadAndCreateTemplate(is);
		} catch (IOException e) {
			log.error(e.getMessage());
		}
		return oshinkoSparkTemplate;
	}
}
