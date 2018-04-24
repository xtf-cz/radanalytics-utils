package cz.xtf.radanalytics.db.mongodb;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeoutException;

import cz.xtf.openshift.OpenShiftUtil;
import cz.xtf.openshift.OpenShiftUtils;
import cz.xtf.radanalytics.db.OpenshiftDB;
import cz.xtf.radanalytics.util.TestHelper;
import cz.xtf.radanalytics.util.configuration.RadanalyticsConfiguration;
import io.fabric8.openshift.api.model.Template;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class MongoDB {
	private static final OpenShiftUtil openshift = OpenShiftUtils.master();

	private static final String DEFAULT_MONGODB_SERVICE_NAME = "mongodb";
	private static final String DEFAULT_MONGODB_ADMIN_USERNAME = "admin";
	private static final String RESOURCES_WORKDIR = "mongodb";

	private static String MONGODB_EPHEMERAL_TEMPLATE = null;


	public static OpenshiftDB deployEphemeral(String mongoDbUser, String mongoDbPassword, String mongoDbDatabase) {

		if (MONGODB_EPHEMERAL_TEMPLATE == null) {
			MONGODB_EPHEMERAL_TEMPLATE = TestHelper.downloadAndGetResources(RESOURCES_WORKDIR, "mongodb-ephemeral-template.json", RadanalyticsConfiguration.TEMPLATE_MONGODB_EPHEMERAL_URL);
		}

		return deployMongoDB(MONGODB_EPHEMERAL_TEMPLATE, null, null, null,
				mongoDbUser, mongoDbPassword, mongoDbDatabase, null, null);
	}

	private static OpenshiftDB deployMongoDB(String templatePath, String memoryLimit, String namespace, String databaseServiceName,
			String mongoDbUser, String mongoDbPassword, String mongoDbDatabase, String mongoDbAdminPassword, String mongoDbVersion) {

		Map<String, String> params = new HashMap<>();
		params.put("MEMORY_LIMIT", memoryLimit);
		params.put("NAMESPACE", namespace);
		params.put("DATABASE_SERVICE_NAME", databaseServiceName);
		params.put("MONGODB_USER", mongoDbUser);
		params.put("MONGODB_PASSWORD", mongoDbPassword);
		params.put("MONGODB_DATABASE", mongoDbDatabase);
		params.put("MONGODB_ADMIN_PASSWORD", mongoDbAdminPassword);
		params.put("MONGODB_VERSION", mongoDbVersion);

		Template template;
		try (InputStream is = Files.newInputStream(Paths.get(templatePath))) {
			log.debug("MongoDB - loading template");
			template = openshift.loadAndCreateTemplate(is);
		} catch (IOException e) {
			log.error("Exception during loading of MongoDB template: {}", e.getMessage());
			throw new IllegalStateException("Wasn't able to load MongoDB template");
		}

		if (databaseServiceName == null) {
			databaseServiceName = DEFAULT_MONGODB_SERVICE_NAME;
		}

		log.info("MongoDB - deploying");
		openshift.processAndDeployTemplate(template.getMetadata().getName(), params);

		try {
			log.debug("Waiting for MongoDB Pod to be ready");
			openshift.waiters().areExactlyNPodsReady(1, "name", databaseServiceName).execute();
		} catch (TimeoutException e) {
			log.error("Timeout exception during creating Pod: {}", e.getMessage());
			throw new IllegalStateException("Timeout expired while waiting for MongoDB availability");
		}
		log.info("MongoDB - deployed");

		OpenshiftDB db = new OpenshiftDB();
		db.setUsername(mongoDbUser);
		db.setPassword(mongoDbPassword);
		db.setAdminUsername(DEFAULT_MONGODB_ADMIN_USERNAME);
		db.setAdminPassword(mongoDbAdminPassword);
		db.setDbName(mongoDbDatabase);
		db.setConnectionUrl("mongodb://" + mongoDbUser + ":" + mongoDbPassword + "@" + databaseServiceName + "/" + mongoDbDatabase);

		return db;
	}
}
