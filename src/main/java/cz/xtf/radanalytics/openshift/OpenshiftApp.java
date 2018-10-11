package cz.xtf.radanalytics.openshift;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import cz.xtf.openshift.OpenShiftUtil;
import cz.xtf.openshift.OpenShiftUtils;
import cz.xtf.radanalytics.waiters.OpenshiftAppsWaiters;
import io.fabric8.kubernetes.api.model.ContainerPort;
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

@Slf4j
public class OpenshiftApp {

	private OpenShiftUtil openshift = OpenShiftUtils.master();
	private List<ContainerPort> containerPortList = new ArrayList<>();

	private String appName;
	private String imageName;
	private String gitUrl;
	private Map<String, String> containerEnvVars;
	private int containerPort;
	private int exposedPort;

	private BuildConfig buildConfig = null;
	private DeploymentConfig deploymentConfig;
	private Service service;
	private ImageStream appImageStream;
	private ImageStream baseImageStream;

	private String baseImageStreamName;

	/**
	 * Creates the Openshift application from base image and container environment variables
	 * Similar way it is done via: oc new-app [baseimage] -e [VARIABLE=value]
	 *
	 * @param appName name of the application to be created
	 * @param imageName base image name which should be used to create this application
	 * @param containerEnvVars environment variables for the container
	 */
	public OpenshiftApp(String appName, String imageName, Map<String, String> containerEnvVars) {
		this(appName, imageName, containerEnvVars, 8080);
	}

	/**
	 * Creates the Openshift application from base image, Git repository and container environment variables
	 * Similar way it is done via: oc new-app [baseimage]~[https://git.repo.url] -e [VARIABLE=value]
	 *
	 * @param appName name of the application to be created
	 * @param imageName base image name which should be used to build and create this application
	 * @param gitUrl repository with sources to be used by the build
	 * @param containerEnvVars environment variables for the container
	 */
	public OpenshiftApp(String appName, String imageName, String gitUrl, Map<String, String> containerEnvVars) {
		this(appName, imageName, containerEnvVars);
		this.gitUrl = gitUrl;

		baseImageStreamName = imageName.substring(imageName.lastIndexOf('/') + 1);
		generateBaseImageStream();

		generateAppImageStream();
		generateBuildConfig();
	}

	/**
	 * Creates the Openshift application from base image and container environment variables
	 * Similar way it is done via: oc new-app [baseimage]~[https://git.repo.url] -e [VARIABLE=value]
	 *
	 * @param appName name of the application to be created
	 * @param imageName base image name which should be used to create this application
	 * @param gitUrl repository with sources to be used by the build
	 * @param containerEnvVars environment variables for the container
	 * @param containerPort environment variables for specifying container port (by default it is 8080)
	 * @param exposedPort environment variables for specifying exposing port
	 */
	public OpenshiftApp(String appName, String imageName, String gitUrl, Map<String, String> containerEnvVars, int containerPort, int exposedPort) {
		this.containerPort = containerPort;
		this.exposedPort = exposedPort;
		this.appName = appName;
		this.imageName = imageName;
		this.gitUrl = gitUrl;
		if (containerEnvVars == null) {
			this.containerEnvVars = new HashMap<>();
		} else {
			this.containerEnvVars = containerEnvVars;
		}

		baseImageStreamName = imageName.substring(imageName.lastIndexOf('/') + 1);
		generateBaseImageStream();
		generateAppImageStream();

		generateBuildConfig();
		generateDeploymentConfig();
		generateService();
	}

	/**
	 * Creates the Openshift application from base image and container environment variables
	 * Similar way it is done via: oc new-app [baseimage] -e [VARIABLE=value]
	 *
	 * @param appName name of the application to be created
	 * @param imageName base image name which should be used to create this application
	 * @param containerEnvVars environment variables for the container
	 * @param containerAndExposedPort environment variables for specifying container port (exposed port will be the same as container port)
	 */
	public OpenshiftApp(String appName, String imageName, Map<String, String> containerEnvVars, int containerAndExposedPort) {
		this(appName, imageName, containerEnvVars, containerAndExposedPort, containerAndExposedPort);
	}

	/**
	 * Creates the Openshift application from base image and container environment variables
	 * Similar way it is done via: oc new-app [baseimage] -e [VARIABLE=value]
	 *
	 * @param appName name of the application to be created
	 * @param imageName base image name which should be used to create this application
	 * @param containerEnvVars environment variables for the container
	 * @param containerPort environment variables for specifying container port (by default it is 8080)
	 * @param exposedPort environment variables for specifying exposing port
	 */
	public OpenshiftApp(String appName, String imageName, Map<String, String> containerEnvVars, int containerPort, int exposedPort) {
		this.containerPort = containerPort;
		this.exposedPort = exposedPort;
		this.appName = appName;
		this.imageName = imageName;
		if (containerEnvVars == null) {
			this.containerEnvVars = new HashMap<>();
		} else {
			this.containerEnvVars = containerEnvVars;
		}

		baseImageStreamName = appName;
		generateBaseImageStream();

		generateDeploymentConfig();
		generateService();
	}

	/**
	 * Deploys (and builds if necessary) the defined application and waits until the build and deployment finishes
	 */
	public void deployAndWaitForFinish() {
		log.info("Starting deployment of app {}.", appName);

		openshift.createImageStream(baseImageStream);
		if (buildConfig != null) {
			openshift.createImageStream(appImageStream);
			openshift.createBuildConfig(buildConfig);
			log.debug("ImageStreams and BuildConfig created");
		}

		openshift.createService(service);
		openshift.createDeploymentConfig(deploymentConfig);
		log.debug("Service and DeploymentConfig created");

		if (buildConfig != null) {
			OpenshiftAppsWaiters.waitForAppBuild(appName);
		}

		OpenshiftAppsWaiters.waitForAppDeployment(appName);

		log.info("Deployment of app {} completed.", appName);
	}

	private void generateDeploymentConfig() {

		this.deploymentConfig = new DeploymentConfigBuilder()
				.withNewMetadata().withName(appName).addToLabels("deploymentConfig", appName).addToLabels("app", appName).endMetadata()
				.withNewSpec()
				.withReplicas(1)
				.withNewStrategy().withType("Rolling").endStrategy()
				.addNewTrigger().withType("ConfigChange").endTrigger()
				.addNewTrigger().withType("ImageChange").withNewImageChangeParams().withAutomatic(true).withContainerNames(appName)
				.withNewFrom().withKind("ImageStreamTag").withName(appName + ":latest").endFrom().endImageChangeParams().endTrigger()
				.addToSelector("deploymentConfig", appName)
				.withNewTemplate().withNewMetadata().addToLabels("deploymentConfig", appName).addToLabels("app", appName).endMetadata()
				.withNewSpec()
				.addNewContainer().withName(appName).withImage(appName)
				.withEnv(containerEnvVars.entrySet().stream().map(entry -> new EnvVar(entry.getKey(), entry.getValue(), null))
						.collect(Collectors.toList()))
				.withPorts(containerPortList)
				.endContainer()
				.withDnsPolicy("ClusterFirst")
				.endSpec()
				.endTemplate()
				.endSpec()
				.build();
	}

	private void generateBuildConfig() {

		this.buildConfig = new BuildConfigBuilder()
				.withNewMetadata().withName(appName).addToLabels("app", appName).endMetadata()
				.withNewSpec()
				.withNewSource().withType("Git").withNewGit().withUri(gitUrl).endGit().endSource()
				.withNewStrategy().withType("Source").withNewSourceStrategy().withNewFrom().withName(baseImageStreamName + ":latest").withKind("ImageStreamTag").endFrom().endSourceStrategy().endStrategy()
				.withNewOutput()
				.withNewTo().withKind("ImageStreamTag").withName(appName + ":latest").endTo()
				.endOutput()
				.addNewTrigger().withType("ImageChange").withNewImageChange().endImageChange().endTrigger()
				.addNewTrigger().withType("ConfigChange").endTrigger()
				.addNewTrigger().withType("GitHub").withNewGithub().withSecret(appName).endGithub().endTrigger()
				.addNewTrigger().withType("Generic").withNewGeneric().withSecret(appName).endGeneric().endTrigger()
				.endSpec().build();
	}

	private void generateService() {

		this.service = new ServiceBuilder()
				.withNewMetadata().withName(appName).addToLabels("app", appName).endMetadata()
				.withNewSpec()
				.addNewPort().withName(exposedPort + "-tcp").withPort(exposedPort).withProtocol("TCP").withNewTargetPort(containerPort).endPort()
				.addToSelector("app", appName)
				.addToSelector("deploymentConfig", appName)
				.endSpec().build();
	}

	private void generateAppImageStream() {

		this.appImageStream = new ImageStreamBuilder()
				.withNewMetadata().withName(appName).addToLabels("app", appName).endMetadata()
				.withNewSpec()
				.withNewLookupPolicy(false)
				.endSpec()
				.build();
	}

	private void generateBaseImageStream() {

		this.baseImageStream = new ImageStreamBuilder()
				.withNewMetadata().withName(baseImageStreamName).addToLabels("app", appName).endMetadata()
				.withNewSpec()
				.addNewTag().withName("latest").addToAnnotations("openshift.io/imported-from", imageName)
				.withNewFrom().withKind("DockerImage").withName(imageName).endFrom()
				.withNewReferencePolicy("Source")
				.endTag()
				.endSpec()
				.build();
	}

	private List<ContainerPort> addPortToList(int port) {
		containerPortList.add(new ContainerPort(port, null, null, null, "TCP"));
		return containerPortList;
	}

	public OpenshiftApp addPort(int newPort) {
		addPortToList(newPort);
		generateDeploymentConfig();
		return this;
	}
}
