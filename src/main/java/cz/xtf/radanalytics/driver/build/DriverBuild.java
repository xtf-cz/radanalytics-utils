package cz.xtf.radanalytics.driver.build;

import cz.xtf.openshift.imagestream.ImageRegistry;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public enum DriverBuild {
	PYTHON_PI(new DriverBuildDefinition("pi", "python", "python-pi", ImageRegistry.get().pySpark(), "pi.py")),
	PYTHON_PI_NO_APP_FILE(new DriverBuildDefinition("pi", "python", "python-pi-noappfile", ImageRegistry.get().pySpark(), null)),
	JAVA_PI(new DriverBuildDefinition("pi", "java", "java-pi", ImageRegistry.get().javaSpark(), "java-spark-pi-1.0-SNAPSHOT.jar")),
	JAVA_PI_NO_APP_FILE(new DriverBuildDefinition("pi", "java", "java-pi-noappfile", ImageRegistry.get().javaSpark(), null)),
	SCALA_PI(new DriverBuildDefinition("pi", "scala", "scala-pi", ImageRegistry.get().scalaSpark(), "sparkpi_2.11-0.1.jar")),
	SCALA_PI_NO_APP_FILE(new DriverBuildDefinition("pi", "scala", "scala-pi-noappfile", ImageRegistry.get().scalaSpark(), null));


	private DriverBuildDefinition driverBuildDefinition;

	public DriverBuildDefinition getDriverBuildDefinition() {
		driverBuildDefinition.createBuilds();
		return driverBuildDefinition;
	}
}
