package cz.xtf.radanalytics.oshinko.deployment.template.python;

import java.util.Map;

import cz.xtf.radanalytics.configuration.RadanalyticsConfiguration;
import cz.xtf.radanalytics.oshinko.deployment.template.BaseTemplateDeployment;

public class OshinkoPythonSpark extends BaseTemplateDeployment {

	private static final String RESOURCES_URL = RadanalyticsConfiguration.templateOshinkoPythonSpark();
	private static final String TEMPLATE_FILE_NAME = "pythonbuilddc.json";

	public static void deploy(Map<String, String> parameters) {
		deploy(RESOURCES_URL, TEMPLATE_FILE_NAME, parameters);
	}

	public static void deploy(Map<String, String> parameters, Map<String, String> envars) {
		deploy(RESOURCES_URL, TEMPLATE_FILE_NAME, parameters, envars);
	}
}
