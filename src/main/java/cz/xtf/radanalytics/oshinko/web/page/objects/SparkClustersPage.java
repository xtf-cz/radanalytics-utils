package cz.xtf.radanalytics.oshinko.web.page.objects;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import java.util.ArrayList;
import java.util.List;

import cz.xtf.radanalytics.waiters.WebWaiters;
import cz.xtf.radanalytics.web.page.objects.AbstractPage;

public class SparkClustersPage extends AbstractPage {

	@FindBy(id = "startbutton")
	private WebElement deployButton;

	private String actionsButton = "//button[@id=\"%s-actions\"]";
	private String scaleClusterDD = "//a[@id=\"%s-scalebutton\"]";  //DD - it's drop down
	private By podsTable = By.xpath("//tbody[@class='ng-scope']");
	private By nameTableCell = By.xpath("./tr/td[@data-title=\"Name\"]");

	//<editor-fold desc="Deploy cluster form">
	@FindBy(id = "cluster-new-name")
	private WebElement clusterNameField;

	@FindBy(id = "cluster-new-workers")
	private WebElement clusterWorkersCountField;

	@FindBy(id = "createbutton")
	private WebElement createButton;
	//</editor-fold>

	//<editor-fold desc="Scale cluster form">
	@FindBy(xpath = "//input[@name=\"nummasters\"]")
	private WebElement mastersToScaleCountField;

	@FindBy(xpath = "//input[@name=\"numworkers\"]")
	private WebElement workersToScaleCountField;

	@FindBy(id = "scalebutton")
	private WebElement scaleButton;

	@FindBy(id = "cancelbutton")
	private WebElement cancellButton;
	//</editor-fold>

	public SparkClustersPage(WebDriver webDriver, String hostname, boolean navigateToPage) {
		super(webDriver, hostname, navigateToPage, "http://" + hostname + "/#/clusters");
	}

	public SparkClustersPage clickOnDeployButton() {
		WebWaiters.waitUntilElementIsVisible(deployButton, webDriver);
		deployButton.click();
		return this;
	}

	public SparkClustersPage fillDeployClusterName(String clusterName) {
		clusterNameField.sendKeys(clusterName);
		return this;
	}

	public SparkClustersPage fillNumberOfWorkers(Integer workersCount) {
		if (workersCount >= 0) {
			clusterWorkersCountField.clear();
			clusterWorkersCountField.sendKeys(Integer.toString(workersCount));
		}
		return this;
	}

	public SparkClustersPage submitDeployClusterForm() {
		createButton.click();
		return this;
	}

	public SparkClustersPage clickOnActionsDD(String clusterName) {
		webDriver.findElement(By.xpath(String.format(actionsButton, clusterName))).click();
		return this;
	}

	public SparkClustersPage chooseItemToScaleCluster(String clusterName) {
		webDriver.findElement(By.xpath(String.format(scaleClusterDD, clusterName))).click();
		return this;
	}

	public SparkClustersPage fillToScaleNumberOfMasters(Integer countMasters) {
		mastersToScaleCountField.clear();
		mastersToScaleCountField.sendKeys(Integer.toString(countMasters));
		return this;
	}

	public SparkClustersPage fillToScaleNumberOfWorkers(Integer countWorkers) {
		workersToScaleCountField.clear();
		workersToScaleCountField.sendKeys(Integer.toString(countWorkers));
		return this;
	}

	public SparkClustersPage clickOnScaleButton() {
		scaleButton.click();
		return this;
	}

	public List<String> getClusterNamesFromTable() {
		List<String> clusterNames = new ArrayList<>();
		webDriver.findElements(podsTable).forEach(x -> clusterNames.add(x.findElement(nameTableCell).getText()));
		return clusterNames;
	}
}
