package cz.xtf.radanalytics.db;

import cz.xtf.openshift.OpenShiftUtil;
import cz.xtf.openshift.OpenShiftUtils;
import cz.xtf.radanalytics.waiters.OpenshiftAppsWaiters;
import io.fabric8.openshift.api.model.Template;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Map;

@Slf4j
public class BaseDBDeployment {
	private static final OpenShiftUtil openshift = OpenShiftUtils.master();

	protected static void deploy(String serviceName, String templatePath, Map<String, String> params) {
		Template template;

		try (InputStream is = Files.newInputStream(Paths.get(templatePath))) {
			log.debug("{} - loading template", serviceName);
			template = openshift.loadAndCreateTemplate(is);
		} catch (IOException e) {
			log.error("Exception during loading of {} template: {}", serviceName, e.getMessage());
			throw new IllegalStateException("Wasn't able to load template");
		}

		log.debug("{}- deploying", serviceName);

		openshift.processAndDeployTemplate(template.getMetadata().getName(), params);

		OpenshiftAppsWaiters.waitForAppDeployment(serviceName);

		log.debug("{}- deployed", serviceName);

	}

	protected static void restartPod(String name) {
		log.info("{} restart Pod - killing the pod", name);
		openshift.deletePods("name", name);
		OpenshiftAppsWaiters.waitForAppDeployment(name);
		log.info("Restart Pod {} - finished", name);
	}
}
