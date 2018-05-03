package cz.xtf.radanalytics.web.webdriver;

import cz.xtf.openshift.OpenShiftUtil;
import cz.xtf.openshift.OpenShiftUtils;
import cz.xtf.radanalytics.oshinko.deployment.Oshinko;
import cz.xtf.radanalytics.util.configuration.RadanalyticsConfiguration;
import io.fabric8.kubernetes.api.model.Service;
import io.fabric8.kubernetes.api.model.ServiceBuilder;
import io.fabric8.openshift.api.model.*;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.TimeoutException;

@Slf4j
public class WebDriverPodBuilder {
	private String webdriver;
	private String podName;
	private static final OpenShiftUtil openshift = OpenShiftUtils.master();

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
		try {
			openshift.waiters().areExactlyNPodsReady(1, "name", podName).execute();
		} catch (TimeoutException e) {
			log.error(e.getMessage());
		}
	}

	public DeploymentConfig deploymentConfig(String podName) {
		getWebdriverBy(podName);
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
				.withName(webdriver)
				.withImage(imageStreamBy(podName))
				.addNewEnv()
				.withName("IGNORE_SSL_ERRORS")
				.withValue("true")
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
				.withNewSecurityContext()
				.withNewCapabilities()
				.withDrop("KILL", "MKNOD", "SETGID", "SETUID")
				.endCapabilities()
				.withPrivileged(false)
				.endSecurityContext()
				.endContainer()
				.withDnsPolicy("ClusterFirst")
				.withServiceAccountName(Oshinko.getDefaultServiceAccountName())
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

	private void getWebdriverBy(String podName) {
		if (podName.equals("headless-chrome")) {
			webdriver = "standalone-chrome";
		} else if (podName.equals("headless-chrome-debug")) {
			webdriver = "standalone-chrome-debug";
		} else if (podName.equals("headless-firefox")) {
			webdriver = "standalone-firefox";
		} else if (podName.isEmpty() || podName == null) {
			log.error("Incorrect webdriver instance name.");
			throw new IllegalArgumentException("Incorrect webdriver instance name.");
		}
	}

	private void generateWebdriverImageStream(String podName) {
		if (podName.equals("headless-chrome")) {
			generateImageStream("standalone-chrome", RadanalyticsConfiguration.imageHeadlessChrome());
		} else if (podName.equals("headless-chrome-debug")) {
			generateImageStream("standalone-chrome-debug", RadanalyticsConfiguration.imageHeadlessChromeDebug());
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
			case "headless-chrome-debug":
				imageName = RadanalyticsConfiguration.imageHeadlessChromeDebug() + ":" + imageVersion;
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
