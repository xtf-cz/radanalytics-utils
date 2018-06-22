package cz.xtf.radanalytics.oshinko.web;

import cz.xtf.radanalytics.oshinko.api.OshinkoAPI;
import cz.xtf.radanalytics.oshinko.entity.SparkCluster;
import cz.xtf.radanalytics.oshinko.web.page.objects.ClusterDetailsPage;
import cz.xtf.radanalytics.oshinko.web.page.objects.SparkClustersPage;
import cz.xtf.radanalytics.web.webdriver.AbstractWebDriver;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;

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
		return createCluster("", "", clusterName, workersCount, DEFAULT_CLUSTER_COUNT, null, null, null, null);
	}

	@Override
	public boolean createCluster(String metrics, String exposeUi, String clusterName, int workersCount, int mastersCount, String masterConfig, String workerConfig, String storedConfig, String sparkImage) {
		log.info("OshinkoPoddedWebUI create cluster with name :{}, worker count : {},  master config : {}, worker config : {}, stored config: {}, spark image : {}",
				clusterName, workersCount, masterConfig, workerConfig, storedConfig, sparkImage);

		return new SparkClustersPage(webDriver, hostname, true)
				.clickOnDeployButton()
				.fillDeployClusterName(clusterName)
				.fillNumberOfWorkers(workersCount)
				.submitDeployClusterForm()
				.isClusterExist();
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
}

