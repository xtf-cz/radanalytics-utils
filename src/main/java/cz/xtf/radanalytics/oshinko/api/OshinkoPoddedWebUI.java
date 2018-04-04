package cz.xtf.radanalytics.oshinko.api;

import cz.xtf.radanalytics.oshinko.entity.SparkPod;
import cz.xtf.radanalytics.oshinko.entity.SparkCluster;

import cz.xtf.wait.WaitUtil;
import cz.xtf.webdriver.GhostDriverService;
import cz.xtf.webdriver.WebDriverService;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.WebDriver;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeoutException;


//todo replace OshinkoPoddedWebUI to standard page object test model ?
@Slf4j
public class OshinkoPoddedWebUI implements OshinkoAPI {

	private final String hostname;
	private final WebDriver webDriver;

	private OshinkoPoddedWebUI(String hostname) {
		this.hostname = hostname;
		log.info("Init OshinkoPoddedWebUI");
		GhostDriverService.get().start();
		webDriver = WebDriverService.get().start();
	}

	public static OshinkoPoddedWebUI getInstance(String hostname) {
		return new OshinkoPoddedWebUI(hostname);
	}

	@Override
	public boolean createCluster(String clusterName) {
		return createCluster(clusterName, -10);
	}

	@Override
	public boolean createCluster(String clusterName, int workersCount) {
		return createCluster("", "", clusterName, workersCount, -10, null, null, null, null);
	}

	@Override
	public boolean createCluster(String metrics, String exposeUi, String clusterName, int workersCount, int mastersCount, String masterConfig, String workerConfig, String storedConfig, String sparkImage) {
		log.debug("OshinkoPoddedWebUI create cluster with name :{}, worker count : {},  master config : {}, worker config : {}, stored config: {}, spark image : {}",
				clusterName, workersCount, masterConfig, workerConfig, storedConfig, sparkImage);

		String url = "http://" + hostname + "/#/clusters";

		By deployButton = By.id("startbutton");
		By clusterNameField = By.id("cluster-new-name");
		By clusterWorkersCountField = By.id("cluster-new-workers");
		By createButton = By.id("createbutton");

		webDriver.navigate().to(url);
		waitFor(deployButton);

		webDriver.findElement(deployButton).click();
		waitFor(clusterNameField);
		waitFor(clusterWorkersCountField);
		waitFor(createButton);

		webDriver.findElement(clusterNameField).sendKeys(clusterName);
		if (workersCount >= 0) {
			webDriver.findElement(clusterWorkersCountField).clear();
			webDriver.findElement(clusterWorkersCountField).sendKeys(Integer.toString(workersCount));
		}
		webDriver.findElement(createButton).click();

		return true;
	}

	@Override
	public SparkCluster getCluster(String clusterName) {
		log.info("OshinkoPoddedWebUI get cluster with name : {}", clusterName);
		String url = "http://" + hostname + "/#/clusters/" + clusterName;

		webDriver.navigate().to(url);

		By nonExistingCluster = By.xpath("//div[@class='well blank-slate-pf spacious ng-scope']/h3");
		if (webDriver.findElements(nonExistingCluster).size() > 0) {
			return null;
		}

		//todo try find better xpath or use css selectors.
		By name = By.xpath("//dl[@class='dl-horizontal']/dt[. = 'Name']/following-sibling::dd");
		By status = By.xpath("//dl[@class='dl-horizontal']/dt[. = 'Status']/following-sibling::dd");
		By master = By.xpath("//dl[@class='dl-horizontal']/dt[. = 'Master']/following-sibling::dd");
		By workerCount = By.xpath("//dl[@class='dl-horizontal']/dt[. = 'Worker count']/following-sibling::dd");
		By podsTable = By.xpath("//tbody[@class='ng-scope']");

		SparkCluster cluster = new SparkCluster();

		cluster.setClusterName(webDriver.findElement(name).getText());
		cluster.setStatus(webDriver.findElement(status).getText());
		cluster.setMasterUrl(webDriver.findElement(master).getText());
		cluster.setWorkersCount(Integer.parseInt(webDriver.findElement(workerCount).getText()));

		List<SparkPod> sparkPods = new ArrayList<>();

		webDriver.findElements(podsTable).forEach(x -> {
			String[] splittedRow = x.getText().split("\\n");
			SparkPod pod = new SparkPod();

			pod.setIp(splittedRow[0]);
			pod.setType(splittedRow[1]);
			// TODO here should be status, however its not showed up in podded web ui

			sparkPods.add(pod);
		});
		cluster.setSparkPods(sparkPods);
		cluster.setMastersCount(((int) sparkPods.stream().filter(x -> x.getType().equals("master")).count()));

		return cluster;
	}

	@Override
	public List<SparkCluster> listClusters() {
		log.info("OshinkoPoddedWebUI get clusters.");
		String url = "http://" + hostname + "/#/clusters";

		By clustersTable = By.xpath("//tbody[@class='ng-scope']");
		List<String> clusterNames = new ArrayList<>();

		webDriver.navigate().to(url);
		webDriver.findElements(clustersTable).forEach(x -> clusterNames.add(x.getText().split(" ")[0]));

		List<SparkCluster> clusters = new ArrayList<>();
		clusterNames.forEach(name -> clusters.add(getCluster(name)));

		return clusters;
	}

	@Override
	public boolean scaleCluster(String clusterName, int workersCount) {
		log.info("OshinkoWPoddedWebUI scale {} to worker count : {}", clusterName, workersCount);
		String url = "http://" + hostname + "/#/clusters/" + clusterName;

		By actionsButton = By.xpath("//button[contains(text(),'Actions')]");
		By scaleAction = By.xpath("//a[contains(text(),'Scale')]");
		By workersCountField = By.name("numworkers");
		By scaleButton = By.id("scalebutton");

		webDriver.navigate().to(url);
		waitFor(actionsButton);

		webDriver.findElement(actionsButton).click();
		waitFor(scaleAction);

		webDriver.findElement(scaleAction).click();
		waitFor(workersCountField);
		waitFor(scaleButton);

		webDriver.findElement(workersCountField).clear();
		webDriver.findElement(workersCountField).sendKeys(Integer.toString(workersCount));
		webDriver.findElement(scaleButton).click();

		return true;
	}

	@Override
	public boolean deleteCluster(String clusterName) {
		log.info("OshinkoWPoddedWebUI delete ", clusterName);
		String url = "http://" + hostname + "/#/clusters/" + clusterName;

		By actionsButton = By.xpath("//button[contains(text(),'Actions')]");
		By deleteAction = By.xpath("//a[contains(text(),'Delete')]");
		By deleteButton = By.id("deletebutton");

		webDriver.navigate().to(url);
		waitFor(actionsButton);

		webDriver.findElement(actionsButton).click();
		waitFor(deleteAction);

		webDriver.findElement(deleteAction).click();
		waitFor(deleteButton);

		webDriver.findElement(deleteButton).click();

		return true;
	}

	private void waitFor(By element) {
		try {
			WaitUtil.waitFor(() -> elementPresent(element), null, 1000L, 20_000);
		} catch (TimeoutException | InterruptedException e) {
			log.error("Timeout exception during waiting for web element: {}", e.getMessage());
			throw new IllegalStateException("Timeout exception during waiting for web element:" + e.getMessage());
		}
	}

	//todo way we don't use explicit wait for elements ?
	private boolean elementPresent(By element) {
		try {
			return webDriver.findElement(element).isDisplayed();
		} catch (NoSuchElementException | StaleElementReferenceException x) {
			return false;
		}
	}
}
