package cz.xtf.radanalytics.oshinko.api;

import cz.xtf.radanalytics.oshinko.entity.SparkCluster;
import cz.xtf.radanalytics.oshinko.entity.SparkConfig;

import java.util.List;

public interface OshinkoAPI {

	boolean createCluster(String clusterName);

	boolean createCluster(String clusterName, int workersCount);

	boolean createCluster(String metrics, String exposeUi, String clusterName, int workersCount, int mastersCount, String masterConfig, String workerConfig, String storedConfig, String sparkImage);

	SparkCluster getCluster(String clusterName);

	List<SparkCluster> listClusters();

	boolean scaleCluster(String clusterName, int workersCount);

	boolean deleteCluster(String clusterName);

	default String checkClusterStatus(String status, String clusterName) {
		return "";
	}

	default boolean createCluster(String clusterName, int workersCount, String imageSpark) {
		return false;
	}

	default SparkConfig getClusterConfig(String clusterName) {
		return new SparkConfig();
	}
}
