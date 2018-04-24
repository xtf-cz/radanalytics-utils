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

	private RadanalyticsConfiguration() {
		super();
	}


	public static String pySpark() {
		return get().readValue(IMAGE_PY_SPARK);
	}

	public static String javaSpark() {
		return get().readValue(IMAGE_JAVA_SPARK);
	}

	public static String scalaSpark() {
		return get().readValue(IMAGE_SCALA_SPARK);
	}

	public static String oshinkoCli() {
		return get().readValue(IMAGE_OSHINKO_CLI);
	}

	public static boolean shouldBuildAll(){
		return Boolean.parseBoolean(get().readValue(SHOULD_BUILD_ALL_DRIVER_APPLICATION));
	}
}

