package cz.xtf.radanalytics.util.configuration;

import cz.xtf.XTFConfiguration;

public class RadanalyticsConfiguration extends XTFConfiguration {
	private static final String IMAGE_PY_SPARK = "rad.driver.python";
	private static final String IMAGE_JAVA_SPARK = "rad.driver.java";
	private static final String IMAGE_SCALA_SPARK = "rad.driver.scala";
	private static final String IMAGE_OSHINKO_CLI = "rad.oshinko.cli";
	private static final String IMAGE_DOCKER_WEBDRIVER_HEADLESS_CHROME = "rad.web-driver.docker.image.chrome";
	private static final String IMAGE_DOCKER_WEBDRIVER_HEADLESS_FIREFOX = "rad.web-driver.docker.image.firefox";
	private static final String IMAGE_DOCKER_WEBDRIVER_VERSION = "rad.web-driver.docker.image.version";
	private static final String WEBDRIVER_BROWSER_NAME = "rad.web-driver.browser.name";  //chrome, firefox, phantomjs

	private static final String SHOULD_BUILD_ALL_DRIVER_APPLICATION = "rad.build.all.drivers";

	private static final String TEMPLATE_OSHINKO_ALL_RESOURCES_URL = "rad.oshinko.all.resources.url";
	private static final String TEMPLATE_OSHINKO_WEBUI_RESOURCES_URL = "rad.oshinko.web.ui.resources.url";
	private static final String TEMPLATE_MONGODB_EPHEMERAL_URL = "rad.mongodb.ephemeral.template.json";
	private static final String TEMPLATE_POSTGRES_EPHEMERAL_URL = "rad.postgresql.ephemeral.template.json";
	private static final String TEMPLATE_POSTGRES_PERSISTENT_URL = "rad.postgresql.persistent.template.json";
	private static final String TEMPLATE_OSHINKO_PYTHON_SPARK_JSON="rad.oshinko.python.spark.template.json";
	private static final String TEMPLATE_OSHINKO_JAVA_SPARK_JSON="rad.oshinko.java.spark.template.json";
	private static final String TEMPLATE_OSHINKO_SCALA_SPARK_JSON="rad.oshinko.scala.spark.template.json";
	private static final String OPENSHIFT_INSTANCE_HOSTNAME_SUFFIX = "rad.openshift.instance.hostname.suffix";

	private RadanalyticsConfiguration() {
		super();
	}

	public static String getWebDriverBrowserName() {
		return get().readValue(WEBDRIVER_BROWSER_NAME);
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

	public static String imageHeadlessChrome() {
		return get().readValue(IMAGE_DOCKER_WEBDRIVER_HEADLESS_CHROME);
	}

	public static String imageHeadlessFirefox() {
		return get().readValue(IMAGE_DOCKER_WEBDRIVER_HEADLESS_FIREFOX);
	}

	public static String imageVersionOfWebdriverDocker() {
		return get().readValue(IMAGE_DOCKER_WEBDRIVER_VERSION);
	}

	public static boolean shouldBuildAll() {
		return Boolean.parseBoolean(get().readValue(SHOULD_BUILD_ALL_DRIVER_APPLICATION));
	}

	public static String templateOshinkoAllResourcesUrl() {
		return get().readValue(TEMPLATE_OSHINKO_ALL_RESOURCES_URL);
	}

	public static String templateOshinkoWebUiResourcesUrl() {
		return get().readValue(TEMPLATE_OSHINKO_WEBUI_RESOURCES_URL);
	}

	public static String templateMongodbEphemeralUrl() {
		return get().readValue(TEMPLATE_MONGODB_EPHEMERAL_URL);
	}

	public static String templatePostgresEphemeralUrl() {
		return get().readValue(TEMPLATE_POSTGRES_EPHEMERAL_URL);
	}

	public static String templatePostgresPersistentUrl() {
		return get().readValue(TEMPLATE_POSTGRES_PERSISTENT_URL);
	}

	public static String templateOshinkoJavaSpark() {
		return get().readValue(TEMPLATE_OSHINKO_JAVA_SPARK_JSON);
	}

	public static String templateOshinkoPythonSpark() {
		return get().readValue(TEMPLATE_OSHINKO_PYTHON_SPARK_JSON);
	}

	public static String templateOshinkoScalaSpark() {
		return get().readValue(TEMPLATE_OSHINKO_SCALA_SPARK_JSON);
	}

	public static String openshiftInstanceHostNameSuffix() {
		return get().readValue(OPENSHIFT_INSTANCE_HOSTNAME_SUFFIX);
	}
}

