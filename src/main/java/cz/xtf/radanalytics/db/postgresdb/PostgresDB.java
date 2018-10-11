package cz.xtf.radanalytics.db.postgresdb;

import java.util.HashMap;
import java.util.Map;

import cz.xtf.openshift.OpenShiftUtil;
import cz.xtf.openshift.OpenShiftUtils;
import cz.xtf.radanalytics.configuration.RadanalyticsConfiguration;
import cz.xtf.radanalytics.db.BaseDBDeployment;
import cz.xtf.radanalytics.db.entity.OpenshiftDB;
import cz.xtf.radanalytics.util.TestHelper;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class PostgresDB extends BaseDBDeployment {

	private static final OpenShiftUtil openshift = OpenShiftUtils.master();
	private static final String DEFAULT_POSTGRE_SERVICE_NAME = "postgresql";
	private static final String POSTGRESQL_PERSISTENT_TEMPLATE_JSON = "postgresql-ephemeral-template.json";
	private static final String RESOURCES_WORKDIR = "db";
	private static String POSTGRES_PERSISTENT_TEMPLATE = null;

	public static OpenshiftDB deployEphemeral(String postgresqlUser, String postgresqlPassword, String postgresqlDatabase) {
		if (POSTGRES_PERSISTENT_TEMPLATE == null) {

			POSTGRES_PERSISTENT_TEMPLATE = TestHelper.downloadAndGetResources(
					RESOURCES_WORKDIR,
					POSTGRESQL_PERSISTENT_TEMPLATE_JSON,
					RadanalyticsConfiguration.templatePostgresEphemeralUrl());
		}
		return deployPostgresDB(POSTGRES_PERSISTENT_TEMPLATE,
				postgresqlUser,
				postgresqlPassword,
				postgresqlDatabase,
				DEFAULT_POSTGRE_SERVICE_NAME,
				null,
				null,
				null,
				null);
	}

	private static OpenshiftDB deployPostgresDB(String templatePath,
			String postgresqlUser,
			String postgresqlPassword,
			String postgresqlDatabase,
			String postgresServiceName,
			String namespace,
			String voluemCapacity,
			String memoryLimit,
			String postgresqlVersion) {
		Map<String, String> params = new HashMap();
		params.put("POSTGRESQL_USER", postgresqlUser);
		params.put("POSTGRESQL_PASSWORD", postgresqlPassword);
		params.put("POSTGRESQL_DATABASE", postgresqlDatabase);
		params.put("DATABASE_SERVICE_NAME", postgresServiceName);
		params.put("NAMESPACE", namespace);
		params.put("VOLUME_CAPACITY", voluemCapacity);
		params.put("MEMORY_LIMIT", memoryLimit);
		params.put("POSTGRESQL_VERSION", postgresqlVersion);

		if (postgresServiceName == null) {
			postgresServiceName = DEFAULT_POSTGRE_SERVICE_NAME;
		}

		deploy(DEFAULT_POSTGRE_SERVICE_NAME, templatePath, params);

		OpenshiftDB db = new OpenshiftDB();
		db.setUsername(postgresqlUser);
		db.setPassword(postgresqlPassword);
		db.setAdminUsername(postgresqlUser);
		db.setAdminPassword(postgresqlPassword);
		db.setDbName(postgresqlDatabase);
		db.setDeploymentConfigName(postgresServiceName);
		db.setConnectionUrl("postgresql://" + DEFAULT_POSTGRE_SERVICE_NAME + ":5432/");
		return db;
	}

	public static void restartPostgres(String name) {
		if (name == null || name.isEmpty()) {
			restartPod(DEFAULT_POSTGRE_SERVICE_NAME);
		} else {
			restartPod(name);
		}
	}
}
