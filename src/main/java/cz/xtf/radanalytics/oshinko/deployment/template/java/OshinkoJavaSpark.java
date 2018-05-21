package cz.xtf.radanalytics.oshinko.deployment.template.java;

import cz.xtf.radanalytics.oshinko.deployment.template.BaseTemplateDeployment;
import cz.xtf.radanalytics.util.configuration.RadanalyticsConfiguration;

import java.util.Map;

public class OshinkoJavaSpark extends BaseTemplateDeployment {

	private static final String RESOURCES_URL = RadanalyticsConfiguration.templateOshinkoJavaSpark();
	private static final String TEMPLATE_FILE_NAME = "javabuilddc.json";

	public static void deploy(Map<String,String> parameters) {
		deploy(RESOURCES_URL, TEMPLATE_FILE_NAME, parameters);
	}
}
