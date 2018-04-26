package cz.xtf.radanalytics.web.webdriver;

import cz.xtf.openshift.OpenShiftUtil;
import cz.xtf.openshift.OpenShiftUtils;
import cz.xtf.radanalytics.oshinko.deployment.Oshinko;
import cz.xtf.radanalytics.util.configuration.RadanalyticsConfiguration;
import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.api.model.PodBuilder;
import io.fabric8.kubernetes.api.model.Service;
import io.fabric8.kubernetes.api.model.ServiceBuilder;
import io.fabric8.openshift.api.model.*;

import java.util.concurrent.TimeoutException;


public class WebDriverPodBuilder {
	private String webdriver;
	private String podName;
	private static final OpenShiftUtil openshift = OpenShiftUtils.master();

	public WebDriverPodBuilder(String podName) {
		this.podName = podName;
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
			e.printStackTrace();
		}
	}

	public DeploymentConfig deploymentConfig(String podName) {
		webdriverBy(podName);
		generateWebdriverImageStream(podName);
		return new DeploymentConfigBuilder()
				.withNewMetadata()
				.withName(podName)
				.addToLabels("deploymentConfig", podName)
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
				.addToSelector("deploymentConfig", podName)
				.addToSelector("name", podName)
				.withNewTemplate()
				.withNewMetadata()
				.addToLabels("deploymentConfig", podName)
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

	private void webdriverBy(String podName) {
		if (podName.equals("headless-chrome")) {
			webdriver = "standalone-chrome";
		} else if (podName.equals("headless-chrome-debug")) {
			webdriver = "standalone-chrome-debug";
		} else if (podName.equals("headless-firefox")) {
			webdriver = "standalone-firefox";
		}
	}

	private void generateWebdriverImageStream(String podName) {
		if (podName.equals("headless-chrome")) {
			generateImageStream("standalone-chrome", RadanalyticsConfiguration.headlessChrome());
		} else if (podName.equals("headless-chrome-debug")) {
			generateImageStream("standalone-chrome-debug", RadanalyticsConfiguration.headlessChromeDebug());
		} else if (podName.equals("headless-firefox")) {
			generateImageStream("standalone-firefox", RadanalyticsConfiguration.headlessFirefox());
		}
	}

	private ImageStream generateImageStream(String webdriver, String dockerImageRepository) {
		return new ImageStreamBuilder()
				.withNewMetadata()
				.withName(webdriver)
				.endMetadata()
				.withNewSpec()
				.addNewTag()
				.withName(RadanalyticsConfiguration.webdriverDockerImageVersion())
				.endTag()
				.withDockerImageRepository(dockerImageRepository)
				.endSpec()
				.withNewStatus().withDockerImageRepository("").endStatus()
				.build();
	}

	private String imageStreamBy(String podName) {
		String imageName = null;
		String imageVersion = RadanalyticsConfiguration.webdriverDockerImageVersion();
		switch (podName) {
			case "headless-chrome":
				imageName = RadanalyticsConfiguration.headlessChrome() + ":" + imageVersion;
				break;
			case "headless-firefox":
				imageName = RadanalyticsConfiguration.headlessFirefox() + ":" + imageVersion;
				break;
			case "headless-chrome-debug":
				imageName = RadanalyticsConfiguration.headlessChromeDebug() + ":" + imageVersion;
				break;
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

	private Pod generatePod() {

		return new PodBuilder()
				.withNewMetadata().withName(podName).addToLabels("name", podName).endMetadata()
				.withNewSpec()
				.addNewContainer()
				.addNewEnv().withName("IGNORE_SSL_ERRORS").withValue("true").endEnv()
				.withImage(imageStreamBy(podName)).withImagePullPolicy("Always").withName(podName)
				.addNewPort().withContainerPort(4444).withName("webdriver").withProtocol("TCP").endPort()
				.addNewPort().withContainerPort(5900).withName("vnc").endPort()
				.withNewSecurityContext().withNewCapabilities().withDrop("KILL", "MKNOD", "SETGID", "SETUID").endCapabilities()
				.withPrivileged(false).endSecurityContext()
				.endContainer()
				.withDnsPolicy("ClusterFirst")
				.endSpec()
				.build();
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
