package cz.xtf.radanalytics.oshinko.web.page.objects;

import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import cz.xtf.radanalytics.oshinko.entity.SparkCluster;
import cz.xtf.radanalytics.oshinko.entity.SparkConfig;
import cz.xtf.radanalytics.oshinko.entity.SparkPod;
import cz.xtf.radanalytics.waiters.WebWaiters;
import cz.xtf.radanalytics.web.extended.elements.elements.Button;
import cz.xtf.radanalytics.web.page.objects.AbstractPage;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ClusterDetailsPage extends AbstractPage {
	private String clusterName;
	private SparkCluster cluster;

	@FindBy(xpath = "//ul[@class=\"dropdown-menu\"]/preceding-sibling::button")
	private WebElement actionsButton;

	@FindBy(xpath = "//ul[@class=\"dropdown-menu\"]//a[contains(@ng-click, \"scaleCluster\")]")
	private WebElement scaleClusterDDItem;

	@FindBy(xpath = "//ul[@class=\"dropdown-menu\"]//a[contains(@ng-click, \"deleteCluster\")]")
	private WebElement deleteClusterDDItem;

	@FindBy(xpath = "//button[@id=\"deletebutton\"]")
	private WebElement deleteButtonPopUp;

	@FindBy(xpath = "//button[@id=\"cancelbutton\"]")
	private WebElement cancelButtonPopUp;

	@FindBy(xpath = "//div[@class=\"blank-slate-pf-icon\"]/following-sibling::h3")
	private WebElement nonExistingCluster;

	@FindBy(xpath = "//dl[@class='dl-horizontal']/dt[. = 'Name']/following-sibling::dd[1]")
	private WebElement clusterDetailsName;

	@FindBy(xpath = "//dl[@class='dl-horizontal']/dt[. = 'Status']/following-sibling::dd[1]")
	private WebElement clusterDetailsStatus;

	@FindBy(xpath = "//dl[@class='dl-horizontal']/dt[. = 'Master']/following-sibling::dd[1]")
	private WebElement clusterDetailsMaster;

	@FindBy(xpath = "//dl[@class='dl-horizontal']/dt[. = 'Worker count']/following-sibling::dd[1]")
	private WebElement clusterDetailsWorkersCount;

	@FindBy(xpath = "//dl[@class='dl-horizontal']/dt[. = 'Master count']/following-sibling::dd[1]")
	private WebElement clusterDetailsMasterCount;

	@FindBy(xpath = "//li[@heading=\"Pods\"]/a")
	private WebElement podsTab;

	@FindBy(xpath = "//thead[@class=\"ng-scope\"]")
	private WebElement thead;

	private By podsTable = By.xpath("//tbody[@class='ng-scope']");

	@FindBy(xpath = "//pre[@class=\"ng-binding\"]")
	private WebElement clusterConfigData;

	@FindBy(xpath = "//dd[@class=\"ng-scope\" and contains(@ng-if,\"getSparkWebUi\")]")
	private Button exposedSparkWebUi;

	public ClusterDetailsPage(WebDriver webDriver, String hostname, String clusterName, boolean navigateToPage) {
		super(webDriver, hostname, navigateToPage, hostname + "/#/clusters/" + clusterName);
		this.clusterName = clusterName;
	}

	public ClusterDetailsPage clickOnActionsButton() {
		WebWaiters.waitUntilElementIsVisible(actionsButton, webDriver);
		actionsButton.click();
		return this;
	}

	public SparkClustersPage chooseScaleItemInDD() {
		scaleClusterDDItem.click();
		return new SparkClustersPage(webDriver, hostname, false);
	}

	public ClusterDetailsPage chooseDeleteItemDD() {
		deleteClusterDDItem.click();
		return this;
	}

	public ClusterDetailsPage clickOnDeleteButtonPopUp() {
		WebWaiters.waitUntilElementIsVisible(deleteButtonPopUp, webDriver);
		deleteButtonPopUp.click();
		return this;
	}

	public boolean isClusterExist() {
		try {
			nonExistingCluster.isDisplayed();
			return false;
		} catch (NoSuchElementException e) {
			log.error("The cluster \"{}\" does not exist: {}", clusterName, e.getMessage());
			return true;
		}
	}

	public SparkCluster setClusterProperties() {
		log.debug("Setup cluster properties");
		cluster = new SparkCluster();
		WebWaiters.waitUntilElementIsVisible(clusterDetailsName, webDriver);
		cluster.setClusterName(clusterDetailsName.getText());
		cluster.setStatus(clusterDetailsStatus.getText());
		cluster.setMasterUrl(clusterDetailsMaster.getText());
		cluster.setMasterCount(Integer.parseInt(clusterDetailsMasterCount.getText()));
		cluster.setWorkerCount(Integer.parseInt(clusterDetailsWorkersCount.getText()));
		cluster.setMasterWebRoute(exposedSparkWebUi.getElement().getText());
		cluster.setSparkConfig(getClusterConfigFromJson());
		return cluster;
	}

	public ClusterDetailsPage switchToPodsTab() {
		podsTab.click();
		WebWaiters.waitUntilElementIsVisible(thead, webDriver);
		return this;
	}

	public SparkCluster setPodsFromTable(SparkCluster cluster) {
		this.cluster = cluster;
		List<SparkPod> sparkPods = new ArrayList<>();
		WebWaiters.waitUntilElementIsVisible(thead, webDriver);
		webDriver.findElements(podsTable).forEach(webElement -> {
			String[] splittedRow = webElement.getText().split("\\n");
			SparkPod pod = new SparkPod();

			pod.setIp(splittedRow[0]);
			pod.setType(splittedRow[1]);
			// TODO here should be status, however its not showed up in podded web ui

			sparkPods.add(pod);
		});
		cluster.setSparkPods(sparkPods);
//		cluster.setMasterCount(((int) sparkPods.stream().filter(x -> x.getType().equals("master")).count()));
		return cluster;
	}

	public SparkConfig getClusterConfigFromJson() {
		WebWaiters.waitUntilElementIsVisible(clusterConfigData, webDriver);
		String clusterConfig = clusterConfigData.getText();
		SparkConfig sparkConfig = null;
		ObjectMapper mapper = new ObjectMapper();
		mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
		try {
			log.info("Search Spark Config in file : {}", clusterConfig);
			sparkConfig = mapper.readValue(clusterConfig, SparkConfig.class);
		} catch (IOException e) {
			log.error("Can't parse {} file to data of SparkConfig : {}", clusterConfig, e.getMessage());
		}
		return sparkConfig;
	}
}
