package cz.xtf.radanalytics.oshinko.web;

import java.util.ArrayList;
import java.util.List;

import cz.xtf.radanalytics.oshinko.api.OshinkoAPI;
import cz.xtf.radanalytics.oshinko.entity.SparkCluster;
import cz.xtf.radanalytics.oshinko.web.page.objects.ClusterDetailsPage;
import cz.xtf.radanalytics.oshinko.web.page.objects.SparkClustersPage;
import cz.xtf.radanalytics.web.webdriver.AbstractWebDriver;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class OshinkoPoddedWebUI extends AbstractWebDriver implements OshinkoAPI {
	private final String hostname;
	private final Integer DEFAULT_CLUSTER_COUNT = -10;

	private OshinkoPoddedWebUI(String hostname) {
		super();
		this.hostname = hostname;
		log.info("Init OshinkoPoddedWebUI");
	}

	public static OshinkoPoddedWebUI getInstance(String hostname) {
		return new OshinkoPoddedWebUI(hostname);
	}

	@Override
	public boolean createCluster(String clusterName) {
		return createCluster(clusterName, DEFAULT_CLUSTER_COUNT);
	}

	@Override
	public boolean createCluster(String clusterName, int workersCount) {
		log.info("OshinkoPoddedWebUI create cluster with name :{}, workers count : {}",
				clusterName, workersCount);

		return new SparkClustersPage(webDriver, hostname, true)
				.clickOnDeployButton()
				.fillDeployClusterName(clusterName)
				.fillNumberOfWorkers(workersCount)
				.submitDeployClusterForm()
				.isClusterSuccessfullyCreated(clusterName);
	}

	@Override
	public boolean createCluster(String metrics, String exposeUi, String clusterName, int workersCount, int mastersCount, String masterConfig, String workerConfig, String storedConfig, String sparkImage) {
		log.info("OshinkoPoddedWebUI create cluster with name :{}, worker count : {},  master config : {}, worker config : {}, stored config: {}, spark image : {}",
				clusterName, workersCount, masterConfig, workerConfig, storedConfig, sparkImage);

		return new SparkClustersPage(webDriver, hostname, true)
				.clickOnDeployButton()
				.clickOnAdvancedClusterConfiguration()
				.fillDeployClusterName(clusterName)
				.fillNumberOfWorkersAdvanced(workersCount)
				.fillStoredClusterConfigurationAdvanced(storedConfig)
				.fillMasterConfigAdvanced(masterConfig)
				.fillWorkerConfigAdvanced(workerConfig)
				.fillSparkImageAdvanced(sparkImage)
				.exposeSparkWebUI(exposeUi)
				.enableSparkMetrics(metrics)
				.submitDeployClusterForm()
				.isClusterSuccessfullyCreated(clusterName);
	}

	@Override
	public SparkCluster getCluster(String clusterName) {
		log.info("OshinkoPoddedWebUI get cluster with name : {}", clusterName);

		ClusterDetailsPage clusterDetailsPage = new ClusterDetailsPage(webDriver, hostname, clusterName, true);
		if (!clusterDetailsPage.isClusterExist()) {
			return null;
		}
		SparkCluster cluster = clusterDetailsPage.setClusterProperties();

		return new ClusterDetailsPage(webDriver, hostname, clusterName, true).switchToPodsTab().setPodsFromTable(cluster);
	}

	@Override
	public List<SparkCluster> listClusters() {
		log.info("OshinkoPoddedWebUI get clusters.");

		SparkClustersPage sparkClustersPage = new SparkClustersPage(webDriver, hostname, true);
		List<String> clusterNames = sparkClustersPage.getClusterNamesFromTable();

		List<SparkCluster> clusters = new ArrayList<>();
		clusterNames.forEach(name -> clusters.add(getCluster(name)));

		return clusters;
	}

	@Override
	public boolean scaleCluster(String clusterName, int workersCount) {
		log.info("OshinkoWPoddedWebUI scale {} to worker count : {}", clusterName, workersCount);

		new ClusterDetailsPage(webDriver, hostname, clusterName, true)
				.clickOnActionsButton()
				.chooseScaleItemInDD()
				.fillToScaleNumberOfWorkers(workersCount)
				.clickOnScaleButton();
		return true;
	}

	@Override
	public boolean deleteCluster(String clusterName) {
		log.info("OshinkoWPoddedWebUI delete ", clusterName);

		new ClusterDetailsPage(webDriver, hostname, clusterName, true)
				.clickOnActionsButton()
				.chooseDeleteItemDD()
				.clickOnDeleteButtonPopUp();
		return true;
	}

	@Override
	public String checkClusterStatus(String status, String clusterName) {
		log.info("Checking cluster status...");
		if (new SparkClustersPage(webDriver, hostname, true).isStatusClusterExist(status, clusterName)) {
			return status;
		} else {
			return "Current cluster does not exist";
		}
	}

	@Override
	public SparkCluster getClusterConfig(String clusterName) {
		log.info("OshinkoPoddedWebUI get cluster configuration with the cluster name : {}", clusterName);

		ClusterDetailsPage clusterDetailsPage = new ClusterDetailsPage(webDriver, hostname, clusterName, true);
		return clusterDetailsPage.setClusterProperties();
	}
}