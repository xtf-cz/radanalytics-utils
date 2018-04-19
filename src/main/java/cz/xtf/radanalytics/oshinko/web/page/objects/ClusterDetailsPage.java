package cz.xtf.radanalytics.oshinko.web.page.objects;

import cz.xtf.radanalytics.oshinko.entity.SparkCluster;
import cz.xtf.radanalytics.oshinko.entity.SparkPod;
import cz.xtf.radanalytics.web.WebWaiters;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Slf4j
public class ClusterDetailsPage  extends AbstractPage {
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

	public ClusterDetailsPage(WebDriver webDriver, String hostname, String clusterName, boolean navigateToPage) {
		super(webDriver, hostname);
		PageFactory.initElements(webDriver, this);
		this.clusterName = clusterName;
		if(navigateToPage) {
			String url = "http://" + hostname + "/#/clusters/" + clusterName;
			if(!Objects.equals(webDriver.getCurrentUrl(), url)) {
				webDriver.get(url);
			}
		}
	}

	public ClusterDetailsPage clickOnActionsButton() {
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
		deleteButtonPopUp.click();
		return this;
	}

	public boolean isClusterExist() {
		try {
			nonExistingCluster.isDisplayed();
			return false;
		} catch (NoSuchElementException e){
			log.error("The cluster \"{}\" does not exist: {}", clusterName, e.getMessage());
			return true;
		}
	}

	public SparkCluster setClusterProperties() {
		cluster = new SparkCluster();
		cluster.setClusterName(clusterDetailsName.getText());
		cluster.setStatus(clusterDetailsStatus.getText());
		cluster.setMasterUrl(clusterDetailsMaster.getText());
		cluster.setMasterCount(Integer.parseInt(clusterDetailsMasterCount.getText()));
		cluster.setWorkerCount(Integer.parseInt(clusterDetailsWorkersCount.getText()));
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
}
