package cz.xtf.radanalytics.oshinko.web.page.objects;

import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BooleanSupplier;

import cz.xtf.radanalytics.waiters.WebWaiters;
import cz.xtf.radanalytics.web.extended.elements.elements.Button;
import cz.xtf.radanalytics.web.extended.elements.elements.TextField;
import cz.xtf.radanalytics.web.page.objects.AbstractPage;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class SparkClustersPage extends AbstractPage {

	private String deployButton = "//button[@id='startbutton']";
	private String actionsButton = "//button[@id=\"%s-actions\"]";
	private String scaleClusterDD = "//a[@id=\"%s-scalebutton\"]";  //DD - it's drop down
	private By podsTable = By.xpath("//tbody[@class='ng-scope']");
	private By nameTableCell = By.xpath("./tr/td[@data-title=\"Name\"]");
	private String clusterStatus = "//a[text()=\"%s\"]/../..//td[@data-title=\"Status\"]/span[@class=\"ng-binding\"]";

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

	private String advancedClusterConrfigurationfield = "//*[@id=\"toggle-adv\"]";

	@FindBy(id = "cluster-adv-workers")
	private TextField workersCountField;

	@FindBy(id = "cluster-config-name")
	private TextField storedClusterConfigurationField;

	@FindBy(id = "cluster-masterconfig-name")
	private TextField masterConrfigField;

	@FindBy(id = "cluster-workerconfig-name")
	private TextField workerConfigurationField;

	@FindBy(id = "cluster-spark-image")
	private TextField sparkImageField;

	@FindBy(id = "exposewebui")
	private WebElement exposeWebui;

	@FindBy(id = "enablemetrics")
	private WebElement enableSparkMetrics;

	public SparkClustersPage(WebDriver webDriver, String hostname, boolean navigateToPage) {
		super(webDriver, hostname, navigateToPage, hostname + "/#/clusters");
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

	public boolean isClusterSuccessfullyCreated(String clusterName) {
		boolean result = false;
		try {
			WebWaiters.waitUntilElementIsPresent(errorMessageClusterAlreadyExist, webDriver, 5);
			webDriver.findElement(By.xpath(errorMessageClusterAlreadyExist)).isDisplayed();
		} catch (NoSuchElementException | TimeoutException e) {
			result = true;
		}
		return result;
	}

	public boolean isStatusClusterExist(String status, String clusterName) {
		BooleanSupplier successCondition = () -> {
			boolean statusResult = false;
			try {
				try {
					Thread.sleep(6 * 1000L);
				} catch (InterruptedException e) {
					log.error(e.getMessage());
				}
				webDriver.navigate().refresh();
				WebWaiters.waitUntilElementIsPresent(String.format(clusterStatus, clusterName), webDriver, 6);
				if (webDriver.findElement(By.xpath(String.format(clusterStatus, clusterName))).getText().trim().equals(status)) {
					statusResult = true;
				}
				return statusResult;
			} catch (java.util.NoSuchElementException | TimeoutException e) {
				return statusResult;
			}
		};

		try {
			WebWaiters.waitFor(successCondition, null, 5000L, 9000L);
		} catch (InterruptedException | java.util.concurrent.TimeoutException e) {
			log.error(e.getMessage());
		}
		return successCondition.getAsBoolean();
	}

	public SparkClustersPage clickOnAdvancedClusterConfiguration() {
		WebWaiters.waitUntilJSReady();
		WebWaiters.waitUntilElementIsPresent(advancedClusterConrfigurationfield, webDriver);
		webDriver.findElement(By.xpath(advancedClusterConrfigurationfield)).click();
		WebWaiters.waitUntilJSReady();
		return this;
	}

	public SparkClustersPage fillNumberOfWorkersAdvanced(int workersCount) {
		if (workersCount >= 0) {
			workersCountField.clear();
			workersCountField.sendKeys(String.valueOf(workersCount));
		}
		return this;
	}

	public SparkClustersPage fillStoredClusterConfigurationAdvanced(String clusterConfigName) {
		storedClusterConfigurationField.clear();
		storedClusterConfigurationField.sendKeys(clusterConfigName);
		return this;
	}

	public SparkClustersPage fillMasterConfigAdvanced(String masterConfigName) {
		masterConrfigField.clear();
		masterConrfigField.sendKeys(masterConfigName);
		return this;
	}

	public SparkClustersPage fillWorkerConfigAdvanced(String workerConfigName) {
		workerConfigurationField.clear();
		workerConfigurationField.sendKeys(workerConfigName);
		return this;
	}

	public SparkClustersPage fillSparkImageAdvanced(String apacheSparkImageName) {
		sparkImageField.clear();
		sparkImageField.sendKeys(apacheSparkImageName);
		return this;
	}

	public SparkClustersPage exposeSparkWebUI(String doExpose) {
		if (Boolean.valueOf(doExpose)) {
			if (exposeWebui.isSelected()) {
				return this;
			} else exposeWebui.click();
		} else if (exposeWebui.isSelected()) {
			exposeWebui.click();
		}
		return this;
	}

	public SparkClustersPage enableSparkMetrics(String doEnable) {
		if (Boolean.valueOf(doEnable)) {
			if (enableSparkMetrics.isSelected()) {
				return this;
			} else enableSparkMetrics.click();
		} else if (enableSparkMetrics.isSelected()) {
			enableSparkMetrics.click();
		}
		return this;
	}
}