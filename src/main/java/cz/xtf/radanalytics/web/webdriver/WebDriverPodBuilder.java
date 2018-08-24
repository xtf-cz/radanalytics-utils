package cz.xtf.radanalytics.web.webdriver;

import cz.xtf.openshift.OpenShiftUtil;
import cz.xtf.openshift.OpenShiftUtils;
import cz.xtf.radanalytics.configuration.RadanalyticsConfiguration;
import cz.xtf.radanalytics.waiters.OpenshiftAppsWaiters;
import io.fabric8.kubernetes.api.model.Service;
import io.fabric8.kubernetes.api.model.ServiceBuilder;
import io.fabric8.openshift.api.model.*;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class WebDriverPodBuilder {
	private static final OpenShiftUtil openshift = OpenShiftUtils.master();
	private String podName;

	public WebDriverPodBuilder(String podName) {
		this.podName = podName;
		log.debug("Pod name is: {}", podName);
		openshift.createService(this.generateService());
		if (podName.contains("debug")) {
			openshift.createService(this.generateVNCService());
			openshift.createRoute(this.generateVNCRoute());
		}
		openshift.createDeploymentConfig(deploymentConfig(podName));
		openshift.createRoute(this.generateRoute());

		OpenshiftAppsWaiters.waitForAppDeployment(podName);
		OpenshiftAppsWaiters.waitForPodStatus(podName, "Running");
	}

	public DeploymentConfig deploymentConfig(String podName) {
		generateWebdriverImageStream(podName);
		return new DeploymentConfigBuilder()
				.withNewMetadata()
				.withName(podName)
				.addToLabels("deploymentconfig", podName)
				.addToLabels("app", podName)
				.addToLabels("name", podName)
				.endMetadata()
				.withNewSpec()
				.withReplicas(1)
				.withNewStrategy()
				.withType("Rolling")
				.endStrategy()
				.addNewTrigger()
				.withType("ConfigChange")
				.endTrigger()
				.addToSelector("deploymentconfig", podName)
				.addToSelector("name", podName)
				.withNewTemplate()
				.withNewMetadata()
				.addToLabels("deploymentconfig", podName)
				.addToLabels("app", podName)
				.addToLabels("name", podName)
				.endMetadata()
				.withNewSpec()
				.addNewContainer()
				.withName(podName)
				.withImage(imageStreamBy(podName))
				.addNewEnv()
				.withName("IGNORE_SSL_ERRORS")
				.withValue("true")
				.endEnv()
				.addNewEnv()
				.withName("VNC_NO_PASSWORD")
				.withValue("yes")
				.endEnv()
				.withImage(imageStreamBy(podName))
				.withImagePullPolicy("Always")
				.withName(podName)
				.addNewPort()
				.withContainerPort(4444)
				.withName("webdriver")
				.withProtocol("TCP")
				.endPort()
				.addNewPort()
				.withContainerPort(5900)
				.withName("vnc")
				.endPort()
				.withNewReadinessProbe()
				.withNewHttpGet()
				.withPath("/wd/hub")
				.withNewPort(4444)
				.withScheme("HTTP")
				.endHttpGet()
				.withInitialDelaySeconds(10)
				.withPeriodSeconds(10)
				.withSuccessThreshold(1)
				.withTimeoutSeconds(1)
				.withFailureThreshold(3)
				.endReadinessProbe()
				.withNewLivenessProbe()
				.withNewHttpGet()
				.withPath("/wd/hub")
				.withNewPort(4444)
				.withScheme("HTTP")
				.endHttpGet()
				.withInitialDelaySeconds(10)
				.withPeriodSeconds(10)
				.withSuccessThreshold(1)
				.withTimeoutSeconds(1)
				.withFailureThreshold(3)
				.endLivenessProbe()
				.withNewSecurityContext()
				.withNewCapabilities()
				.withDrop("KILL", "MKNOD", "SETGID", "SETUID")
				.endCapabilities()
				.withPrivileged(false)
				.endSecurityContext()
				.endContainer()
				.withDnsPolicy("ClusterFirst")
				.addNewVolume()
				.withName("podinfo")
				.withNewDownwardAPI()
				.addNewItem()
				.withNewFieldRef()
				.withFieldPath("metadata.labels")
				.endFieldRef()
				.withPath("labels")
				.endItem()
				.endDownwardAPI()
				.endVolume()
				.endSpec()
				.endTemplate()
				.endSpec()
				.build();
	}

	private void generateWebdriverImageStream(String podName) {
		if (podName.equals("headless-chrome")) {
			generateImageStream("standalone-chrome", RadanalyticsConfiguration.imageHeadlessChrome());
		} else if (podName.equals("headless-firefox")) {
			generateImageStream("standalone-firefox", RadanalyticsConfiguration.imageHeadlessFirefox());
		}
	}

	private ImageStream generateImageStream(String webdriver, String dockerImageRepository) {
		return new ImageStreamBuilder()
				.withNewMetadata()
				.withName(webdriver)
				.endMetadata()
				.withNewSpec()
				.addNewTag()
				.withName(RadanalyticsConfiguration.imageVersionOfWebdriverDocker())
				.endTag()
				.withDockerImageRepository(dockerImageRepository)
				.endSpec()
				.withNewStatus().withDockerImageRepository("").endStatus()
				.build();
	}

	private String imageStreamBy(String podName) {
		String imageName;
		String imageVersion = RadanalyticsConfiguration.imageVersionOfWebdriverDocker();
		switch (podName) {
			case "headless-chrome":
				imageName = RadanalyticsConfiguration.imageHeadlessChrome() + ":" + imageVersion;
				break;
			case "headless-firefox":
				imageName = RadanalyticsConfiguration.imageHeadlessFirefox() + ":" + imageVersion;
				break;
			default:
				log.error("Webdriver image stream not found.");
				throw new IllegalArgumentException("Webdriver image stream not found.");
		}
		return imageName;
	}

	private Service generateService() {
		return new ServiceBuilder()
				.withNewMetadata().withName(podName).addToLabels("name", podName).endMetadata()
				.withNewSpec()
				.addNewPort().withPort(80).withProtocol("TCP").withNewTargetPort(4444).endPort()
				.addToSelector("name", podName)
				.endSpec().build();
	}

	private Service generateVNCService() {
		return new ServiceBuilder()
				.withNewMetadata().withName(podName + "-vnc").addToLabels("name", podName + "-vnc").endMetadata()
				.withNewSpec()
				.addNewPort().withPort(5901).withNewTargetPort(5900).endPort()
				.addToSelector("name", podName)
				.endSpec().build();
	}

	private Route generateRoute() {
		return new RouteBuilder()
				.withNewMetadata()
				.addToLabels("name", podName)
				.withName(podName)
				.endMetadata()
				.withNewSpec()
				.withNewTo()
				.withKind("Service")
				.withName(podName)
				.withWeight(100)
				.endTo()
				.endSpec()
				.build();
	}

	private Route generateVNCRoute() {
		return new RouteBuilder()
				.withNewMetadata()
				.addToLabels("name", podName + "-vnc")
				.withName(podName + "-vnc")
				.endMetadata()
				.withNewSpec()
				.withNewTo()
				.withKind("Service")
				.withName(podName + "-vnc")
				.withWeight(100)
				.endTo()
				.endSpec()
				.build();
	}
}
