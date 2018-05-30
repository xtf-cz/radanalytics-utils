package cz.xtf.radanalytics.db.mongodb;

import cz.xtf.openshift.OpenShiftUtil;
import cz.xtf.openshift.OpenShiftUtils;
import cz.xtf.radanalytics.db.BaseDBDeployment;
import cz.xtf.radanalytics.db.entity.OpenshiftDB;
import cz.xtf.radanalytics.util.TestHelper;
import cz.xtf.radanalytics.util.configuration.RadanalyticsConfiguration;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Map;

@Slf4j
public class MongoDB extends BaseDBDeployment {
	private static final OpenShiftUtil openshift = OpenShiftUtils.master();
	private static final String DEFAULT_MONGODB_SERVICE_NAME = "mongodb";
	private static final String DEFAULT_MONGODB_ADMIN_USERNAME = "admin";
	private static final String RESOURCES_WORKDIR = "db";
	private static String MONGODB_EPHEMERAL_TEMPLATE = null;

	public static OpenshiftDB deployEphemeral(String mongoDbUser, String mongoDbPassword, String mongoDbDatabase) {
		return deployEphemeral(mongoDbUser, mongoDbPassword, mongoDbDatabase, null);
	}

	public static OpenshiftDB deployEphemeral(String mongoDbUser, String mongoDbPassword, String mongoDbDatabase, String mongoDbAdminPassword) {
		if (MONGODB_EPHEMERAL_TEMPLATE == null) {
			MONGODB_EPHEMERAL_TEMPLATE = TestHelper.downloadAndGetResources(RESOURCES_WORKDIR,
					"mongodb-ephemeral-template.json",
					RadanalyticsConfiguration.templateMongodbEphemeralUrl());
		}
		return deployMongoDB(MONGODB_EPHEMERAL_TEMPLATE, null, null, null,
				mongoDbUser, mongoDbPassword, mongoDbDatabase, mongoDbAdminPassword, null);
	}

	private static OpenshiftDB deployMongoDB(String templatePath,
	                                         String memoryLimit,
	                                         String namespace,
	                                         String databaseServiceName,
	                                         String mongoDbUser,
	                                         String mongoDbPassword,
	                                         String mongoDbDatabase,
	                                         String mongoDbAdminPassword,
	                                         String mongoDbVersion) {

		Map<String, String> params = new HashMap<>();
		params.put("MEMORY_LIMIT", memoryLimit);
		params.put("NAMESPACE", namespace);
		params.put("DATABASE_SERVICE_NAME", databaseServiceName);
		params.put("MONGODB_USER", mongoDbUser);
		params.put("MONGODB_PASSWORD", mongoDbPassword);
		params.put("MONGODB_DATABASE", mongoDbDatabase);
		params.put("MONGODB_ADMIN_PASSWORD", mongoDbAdminPassword);
		params.put("MONGODB_VERSION", mongoDbVersion);

		if (databaseServiceName == null) {
			databaseServiceName = DEFAULT_MONGODB_SERVICE_NAME;
		}

		deploy(databaseServiceName, templatePath, params);

		OpenshiftDB db = new OpenshiftDB();
		db.setUsername(mongoDbUser);
		db.setPassword(mongoDbPassword);
		db.setAdminUsername(DEFAULT_MONGODB_ADMIN_USERNAME);
		db.setAdminPassword(mongoDbAdminPassword);
		db.setDbName(mongoDbDatabase);
		db.setDeploymentConfigName(databaseServiceName);
		db.setConnectionUrl("mongodb://" + mongoDbUser + ":" + mongoDbPassword + "@" + databaseServiceName + "/" + mongoDbDatabase);

		return db;
	}

	public static void restartMongoDb(String name) {
		if (name == null || name.isEmpty()) {
			restartPod(DEFAULT_MONGODB_SERVICE_NAME);
		} else {
			restartPod(name);
		}
	}
}
