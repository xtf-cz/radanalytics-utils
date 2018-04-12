package cz.xtf.radanalytics.webUtils;

import cz.xtf.TestConfiguration;
import cz.xtf.openshift.OpenShiftUtil;
import cz.xtf.openshift.OpenShiftUtils;
import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.api.model.PodBuilder;
import io.fabric8.kubernetes.api.model.Service;
import io.fabric8.kubernetes.api.model.ServiceBuilder;
import io.fabric8.openshift.api.model.Route;
import io.fabric8.openshift.api.model.RouteBuilder;

import java.util.concurrent.TimeoutException;

public class WebDriverPodBuilder {
	private String podName;
	private static final OpenShiftUtil openshift = OpenShiftUtils.master();

	public WebDriverPodBuilder(String podName){
		this.podName = podName;
		openshift.createService(this.generateService());
		if (podName.contains("debug")) {
			openshift.createService(this.generateVNCService());
			String hostNameVNCRoute = String.format("%s%s%s%s%s", podName+"-vnc", ".", openshift.client().getNamespace(), ".", TestConfiguration.routeDomain());
			openshift.createRoute(this.generateVNCRoute(hostNameVNCRoute));
		}
		openshift.createPod(this.generatePod());
		String hostNameRoute = String.format("%s%s%s%s%s", podName, ".", openshift.client().getNamespace(), ".", TestConfiguration.routeDomain());
		openshift.createRoute(this.generateRoute(hostNameRoute));
		try {
			openshift.waiters().areExactlyNPodsReady(1, "name", podName).execute();
		} catch (TimeoutException e) {
			e.printStackTrace();
		}
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
				.withNewMetadata().withName(podName+"-vnc").addToLabels("name", podName+"-vnc").endMetadata()
				.withNewSpec()
				.addNewPort().withPort(5901).withNewTargetPort(5900).endPort()
				.addToSelector("name", podName)
				.endSpec().build();
	}

	private Pod generatePod() {
		String imageName = null;
		String imageVersion = "3.11.0-californium";
		switch (podName) {
			case "headless-chrome":
				imageName = "selenium/standalone-chrome:" + imageVersion;
				break;
			case "headless-firefox":
				imageName = "selenium/standalone-firefox:" + imageVersion;
				break;
			case "headless-chrome-debug":
				imageName = "selenium/standalone-chrome-debug:" + imageVersion;
				break;
		}
		return new PodBuilder()
				.withNewMetadata().withName(podName).addToLabels("name", podName).endMetadata()
				.withNewSpec()
				.addNewContainer()
				.addNewEnv().withName("IGNORE_SSL_ERRORS").withValue("true").endEnv()
				.withImage(imageName).withImagePullPolicy("Always").withName(podName)
				.addNewPort().withContainerPort(4444).withName("webdriver").withProtocol("TCP").endPort()
				.addNewPort().withContainerPort(5900).withName("vnc").endPort()
				.withNewSecurityContext().withNewCapabilities().withDrop("KILL", "MKNOD", "SETGID", "SETUID").endCapabilities()
				.withPrivileged(false).endSecurityContext()
				.endContainer()
				.withDnsPolicy("ClusterFirst")
				.endSpec()
				.build();
	}

	private Route generateRoute(String hostName) {
		return new RouteBuilder().withNewMetadata().addToLabels("name", podName).withName(podName).endMetadata()
				.withNewSpec().withHost(hostName).withNewTo().withKind("Service").withName(podName).withWeight(100).endTo().endSpec()
				.build();
	}

	private Route generateVNCRoute(String hostName) {
		return new RouteBuilder().withNewMetadata().addToLabels("name", podName+"-vnc").withName(podName+"-vnc").endMetadata()
				.withNewSpec().withHost(hostName)
				.withNewTo().withKind("Service").withName(podName+"-vnc").withWeight(100).endTo().endSpec()
				.build();
	}
}
