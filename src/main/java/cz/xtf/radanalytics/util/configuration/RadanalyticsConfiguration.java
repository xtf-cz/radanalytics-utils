package cz.xtf.radanalytics.util.configuration;

import cz.xtf.XTFConfiguration;

public class RadanalyticsConfiguration extends XTFConfiguration {
	private static final String IMAGE_PY_SPARK = "rad.driver.python";
	private static final String IMAGE_JAVA_SPARK = "rad.driver.java";
	private static final String IMAGE_SCALA_SPARK = "rad.driver.scala";
	private static final String IMAGE_OSHINKO_REST = "rad.oshinko.rest";
	private static final String IMAGE_OSHINKO_CLI = "rad.oshinko.cli";
	private static final String IMAGE_OSHINKO_WEBUI = "rad.oshinko.webui";
	private static final String SHOULD_BUILD_ALL_DRIVER_APPLICATION = "rad.build.all.drivers";

	private static final String TEMPLATE_OSHINKO_WEBUI_RESOURCES_URL = "rad.oshinko.web.ui.resources.url";
	private static final String TEMPLATE_MONGODB_EPHEMERAL_URL = "rad.mongodb.ephemeral.template.json";


	private RadanalyticsConfiguration() {
		super();
	}


	public static String imagePySpark() {
		return get().readValue(IMAGE_PY_SPARK);
	}

	public static String imageJavaSpark() {
		return get().readValue(IMAGE_JAVA_SPARK);
	}

	public static String imageScalaSpark() {
		return get().readValue(IMAGE_SCALA_SPARK);
	}

	public static String imageOshinkoCli() {
		return get().readValue(IMAGE_OSHINKO_CLI);
	}

	public static boolean shouldBuildAll() {
		return Boolean.parseBoolean(get().readValue(SHOULD_BUILD_ALL_DRIVER_APPLICATION));
	}

	public static String templateOshinkoWebUiResourcesUrl() {
		return get().readValue(TEMPLATE_OSHINKO_WEBUI_RESOURCES_URL);
	}

	public static String templateMongodbEphemeralUrl() {
		return get().readValue(TEMPLATE_MONGODB_EPHEMERAL_URL);
	}
}

