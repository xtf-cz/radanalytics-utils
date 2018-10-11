package cz.xtf.radanalytics.driver.deployment;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import cz.xtf.TestConfiguration;
import cz.xtf.radanalytics.driver.build.DriverBuild;
import cz.xtf.radanalytics.driver.build.DriverBuildDefinition;
import cz.xtf.radanalytics.oshinko.deployment.Oshinko;
import io.fabric8.kubernetes.api.model.EnvVar;
import io.fabric8.kubernetes.api.model.Service;
import io.fabric8.kubernetes.api.model.ServiceBuilder;
import io.fabric8.openshift.api.model.BuildConfig;
import io.fabric8.openshift.api.model.BuildConfigBuilder;
import io.fabric8.openshift.api.model.DeploymentConfig;
import io.fabric8.openshift.api.model.DeploymentConfigBuilder;
import io.fabric8.openshift.api.model.ImageStream;
import io.fabric8.openshift.api.model.ImageStreamBuilder;
import lombok.extern.slf4j.Slf4j;

// TODO configuring dc x jobs
@Slf4j
public class DriverBuilder {

	private String appName;
	private String gitUrl;
	private String gitRef;
	private String gitContextDir;
	private String imageName;
	private String appFile;

	private BuildConfig buildConfig;
	private DeploymentConfig deploymentConfig;
	private Service service;
	private ImageStream imageStream;
	private Service headlessService;

	private Map<String, String> dcEnvs = new HashMap<>();

	private boolean fromDriverBuildDefinition = false;
	private DriverBuildDefinition driverBuildDefinition;

	public DriverBuilder(String appName, String gitUrl, String imageName, String appFile) {
		this(appName, gitUrl, null, null, imageName, appFile);
	}

	public DriverBuilder(String appName, String gitUrl, String gitRef, String gitContextDir, String imageName, String appFile) {
		log.debug("Creating and deploying the project \"{}\" using the source \"{}\", image \"{}\", appFile \"{}\"", appName, gitUrl, imageName, appFile);
		this.appName = appName;
		this.gitUrl = gitUrl;
		this.gitRef = gitRef;
		this.gitContextDir = gitContextDir;
		this.imageName = imageName;
		this.appFile = appFile;

		dcEnvs.put("DRIVER_HOST", appName + "-headless");
	}

	public DriverBuilder(DriverBuild driverBuild, String appName) {

		this.driverBuildDefinition = driverBuild.getDriverBuildDefinition();
		this.appName = appName;

		this.fromDriverBuildDefinition = true;

		dcEnvs.put("DRIVER_HOST", appName + "-headless");
	}

	public DriverBuilder addEnv(String key, String value) {
		this.dcEnvs.put(key, value);
		return this;
	}

	public DriverBuilder setSparkClusterName(String clusterName) {
		dcEnvs.put("OSHINKO_CLUSTER_NAME", clusterName);
		return this;
	}

	public DriverBuilder setAppArgs(String args) {
		dcEnvs.put("APP_ARGS", args);
		return this;
	}

	public DriverBuilder setSparkArgs(String args) {
		dcEnvs.put("SPARK_OPTIONS", args);
		return this;
	}

	public DriverBuilder setAppMainClass(String appMainClass) {
		dcEnvs.put("APP_MAIN_CLASS", appMainClass);
		return this;
	}

	public DriverBuilder setOshinkoDelCluster(Boolean delCluster) {
		dcEnvs.put("OSHINKO_DEL_CLUSTER", delCluster.toString());
		return this;
	}

	public DriverBuilder setAppExit(Boolean appExit) {
		dcEnvs.put("APP_EXIT", appExit.toString());
		return this;
	}

	public DriverBuilder setOshinkoNamedConfig(String oshinkoNamedConfig) {
		dcEnvs.put("OSHINKO_NAMED_CONFIG", oshinkoNamedConfig);
		return this;
	}

	public DriverBuilder setOshinkoSparkDriverConfig(String oshinkoSparkDriverConfig) {
		dcEnvs.put("OSHINKO_SPARK_DRIVER_CONFIG", oshinkoSparkDriverConfig);
		return this;
	}

	public Driver build() {

		if (fromDriverBuildDefinition) {
			this.imageStream = driverBuildDefinition.getImageStream();
			this.buildConfig = driverBuildDefinition.getBuildConfig();
		} else {
			this.imageStream = GenerateImageStream();
			this.buildConfig = GenerateBuildConfig();
		}
		generateDeploymentConfig();
		generateService();
		generateHeadlessService();

		return new Driver(buildConfig, deploymentConfig, imageStream, service, fromDriverBuildDefinition, headlessService);
	}

	public BuildConfig GenerateBuildConfig() {

		Map<String, String> env = new HashMap<>();

		if (appFile != null) {
			env.put("APP_FILE", appFile);
		}
		if (TestConfiguration.mavenProxyEnabled()) {
			env.put("MAVEN_MIRROR_URL", TestConfiguration.mavenProxyURL());
		}

		return new BuildConfigBuilder()
				.withNewMetadata().withName(appName).addToLabels("app", appName).endMetadata()
				.withNewSpec()
				.withNewSource().withType("Git").withNewGit().withUri(gitUrl).withRef(gitRef).endGit().withContextDir(gitContextDir).endSource()
				.withNewStrategy().withType("Source").withNewSourceStrategy().withNewFrom().withName(imageName).withKind("DockerImage").endFrom().withForcePull(true)
				.withEnv(env.entrySet().stream().map(entry -> new EnvVar(entry.getKey(), entry.getValue(), null))
						.collect(Collectors.toList())).endSourceStrategy()
				.endStrategy()
				.withNewOutput()
				.withNewTo().withKind("ImageStreamTag").withName(appName + ":latest").endTo()
				.endOutput()
				.addNewTrigger().withType("ImageChange").withNewImageChange().endImageChange().endTrigger()
				.addNewTrigger().withType("ConfigChange").endTrigger()
				.addNewTrigger().withType("GitHub").withNewGithub().withSecret(appName).endGithub().endTrigger()
				.addNewTrigger().withType("Generic").withNewGeneric().withSecret(appName).endGeneric().endTrigger()
				.endSpec().build();
	}

	public ImageStream GenerateImageStream() {

		return new ImageStreamBuilder()
				.withNewMetadata().withName(appName).addToLabels("app", appName).endMetadata()
				.withNewSpec()
				.addNewTag().withName("latest").endTag()
				.withDockerImageRepository(appName)
				.endSpec()
				.build();
	}

	private void generateDeploymentConfig() {

		String imageStreamName;
		String imageStreamNamespaceName;
		if (fromDriverBuildDefinition) {
			imageStreamName = driverBuildDefinition.getBuildName();
			imageStreamNamespaceName = TestConfiguration.buildNamespace();
		} else {
			imageStreamName = appName;
			imageStreamNamespaceName = TestConfiguration.masterNamespace();
		}

		this.deploymentConfig = new DeploymentConfigBuilder()
				.withNewMetadata().withName(appName).addToLabels("deploymentConfig", appName).addToLabels("app", appName).endMetadata()
				.withNewSpec()
				.withReplicas(1)
				.withNewStrategy().withType("Rolling").endStrategy()
				.addNewTrigger().withType("ConfigChange").endTrigger()
				.addNewTrigger().withType("ImageChange").withNewImageChangeParams().withAutomatic(true).withContainerNames(appName)
				.withNewFrom().withKind("ImageStreamTag").withName(imageStreamName + ":latest").withNamespace(imageStreamNamespaceName).endFrom().endImageChangeParams().endTrigger()
				.addToSelector("deploymentConfig", appName)
				.withNewTemplate().withNewMetadata().addToLabels("deploymentConfig", appName).addToLabels("app", appName).endMetadata()
				.withNewSpec()
				.addNewContainer().withName(appName).withImage(imageStreamName)
				.withEnv(dcEnvs.entrySet().stream().map(entry -> new EnvVar(entry.getKey(), entry.getValue(), null))
						.collect(Collectors.toList()))
				.addNewEnv().withName("POD_NAME").withNewValueFrom().withNewFieldRef().withFieldPath("metadata.name").endFieldRef().endValueFrom().endEnv()
				.addNewVolumeMount().withMountPath("/etc/podinfo").withName("podinfo").withReadOnly(false).endVolumeMount()
				.endContainer()
				.withDnsPolicy("ClusterFirst")
				.withServiceAccountName(Oshinko.getDefaultServiceAccountName())
				.addNewVolume().withName("podinfo")
				.withNewDownwardAPI().addNewItem().withNewFieldRef().withFieldPath("metadata.labels").endFieldRef().withPath("labels").endItem().endDownwardAPI()
				.endVolume()
				.endSpec()
				.endTemplate()
				.endSpec()
				.build();
	}

	private void generateService() {

		this.service = new ServiceBuilder()
				.withNewMetadata().withName(appName).addToLabels("app", appName).endMetadata()
				.withNewSpec()
				.addNewPort().withName("8080-tcp").withPort(8080).withProtocol("TCP").withNewTargetPort(8080).endPort()
				.addToSelector("deploymentConfig", appName)
				.endSpec().build();
	}

	private void generateHeadlessService() {
		this.headlessService = new ServiceBuilder()
				.withNewMetadata().withName(appName + "-headless").addToLabels("app", appName).endMetadata()
				.withNewSpec()
				.withClusterIP("None")
				.addNewPort().withName("driver-rpc-port").withPort(7078).withProtocol("TCP").withNewTargetPort(7078).endPort()
				.addNewPort().withName("blockmanager").withPort(7079).withProtocol("TCP").withNewTargetPort(7079).endPort()
				.addToSelector("deploymentConfig", appName)
				.endSpec().build();
	}
}
