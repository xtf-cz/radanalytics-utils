package cz.xtf.radanalytics.oshinko.web.page.objects;

import cz.xtf.radanalytics.waiters.WebWaiters;
import cz.xtf.radanalytics.web.extended.elements.elements.Button;
import cz.xtf.radanalytics.web.extended.elements.elements.TextField;
import cz.xtf.radanalytics.web.page.objects.AbstractPage;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.FindBy;

import java.util.ArrayList;
import java.util.List;

public class SparkClustersPage extends AbstractPage {


	private String deployButton = "//button[@id='startbutton']";
	private String actionsButton = "//button[@id=\"%s-actions\"]";
	private String scaleClusterDD = "//a[@id=\"%s-scalebutton\"]";  //DD - it's drop down
	private By podsTable = By.xpath("//tbody[@class='ng-scope']");
	private By nameTableCell = By.xpath("./tr/td[@data-title=\"Name\"]");

	//<editor-fold desc="Deploy cluster form">
	@FindBy(id = "cluster-new-name")
	private TextField clusterNameField;

	@FindBy(id = "cluster-new-workers")
	private TextField clusterWorkersCountField;

	@FindBy(id = "createbutton")
	private Button createButton;

	private String errorMessageClusterAlreadyExist = "//*[@class='alert alert-danger dialog-error']";
	//</editor-fold>

	//<editor-fold desc="Scale cluster form">
	@FindBy(xpath = "//input[@name=\"nummasters\"]")
	private TextField mastersToScaleCountField;

	@FindBy(xpath = "//input[@name=\"numworkers\"]")
	private TextField workersToScaleCountField;

	@FindBy(id = "scalebutton")
	private Button scaleButton;

	@FindBy(id = "cancelbutton")
	private Button cancellButton;
	//</editor-fold>

	public SparkClustersPage(WebDriver webDriver, String hostname, boolean navigateToPage) {
		super(webDriver, hostname, navigateToPage, "http://" + hostname + "/#/clusters");
	}

	public SparkClustersPage clickOnDeployButton() {
		WebWaiters.waitUntilJSReady();
		WebWaiters.waitUntilElementIsPresent(deployButton, webDriver);
		webDriver.findElement(By.xpath(deployButton)).click();
		WebWaiters.waitUntilJSReady();
		return this;
	}

	public SparkClustersPage fillDeployClusterName(String clusterName) {
		WebWaiters.waitUntilElementIsVisible(clusterNameField.getElement(), webDriver);
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

	public boolean isClusterExist() {
		boolean result = false;
		try {
			WebWaiters.waitUntilElementIsPresent(errorMessageClusterAlreadyExist, webDriver, 5);
			webDriver.findElement(By.xpath(errorMessageClusterAlreadyExist)).getText().equals("configmaps \"create-create-metrics\" already exists");
		} catch (NoSuchElementException e) {
			result = true;
		}
		return result;
	}
}
