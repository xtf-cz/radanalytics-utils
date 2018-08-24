package cz.xtf.radanalytics.driver.build;


import cz.xtf.radanalytics.configuration.RadanalyticsConfiguration;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public enum DriverBuild {
	PYTHON_PI(new DriverBuildDefinition("pi", "python", "python-pi", RadanalyticsConfiguration.imagePySpark(), "pi.py")),
	PYTHON_PI_NO_APP_FILE(new DriverBuildDefinition("pi", "python", "python-pi-noappfile", RadanalyticsConfiguration.imagePySpark(), null)),
	JAVA_PI(new DriverBuildDefinition("pi", "java", "java-pi", RadanalyticsConfiguration.imageJavaSpark(), "java-spark-pi-1.0-SNAPSHOT.jar")),
	JAVA_PI_NO_APP_FILE(new DriverBuildDefinition("pi", "java", "java-pi-noappfile", RadanalyticsConfiguration.imageJavaSpark(), null)),
	SCALA_PI(new DriverBuildDefinition("pi", "scala", "scala-pi", RadanalyticsConfiguration.imageScalaSpark(), "sparkpi_2.11-0.1.jar")),
	SCALA_PI_NO_APP_FILE(new DriverBuildDefinition("pi", "scala", "scala-pi-noappfile", RadanalyticsConfiguration.imageScalaSpark(), null));


	private DriverBuildDefinition driverBuildDefinition;

	public DriverBuildDefinition getDriverBuildDefinition() {
		driverBuildDefinition.createBuilds();
		return driverBuildDefinition;
	}
}
