package cz.xtf.radanalytics.notebook.jupyter.page.objects;

import cz.xtf.radanalytics.web.page.objects.AbstractPage;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

@Slf4j
public class JupiterTreePage extends AbstractPage {
	@FindBy(xpath = "//ul[@id=\"tabs\"]//a[contains(text(), \"Files\")]")
	private WebElement filesTab;

	@FindBy(xpath = "//ul[@id=\"tabs\"]//a[contains(text(), \"Running\")]")
	private WebElement runningTab;

	@FindBy(xpath = "//ul[@id=\"tabs\"]//a[contains(text(), \"Clusters\")]")
	private WebElement clustersTab;

	@FindBy(xpath = "//ul[@id=\"tabs\"]//li[@class=\"active\"]/a")
	private WebElement getActiveTab;

	private String projectLink = "//div[@id=\"%s\"]//span[contains(text(), \"%s\")]";

	public JupiterTreePage(WebDriver webDriver, String hostname, boolean navigateToPage) {
		super(webDriver, hostname, navigateToPage, "http://" + hostname + "/tree");
	}

	public JupiterTreePage clickOnFilesTab() {
		filesTab.click();
		return this;
	}

	public JupiterTreePage clickOnRunningTab() {
		runningTab.click();
		return this;
	}

	public JupiterTreePage clickOnClustersTab() {
		clustersTab.click();
		return this;
	}

	public JupiterTreePage chooseProjectInTree(String projectName) {
		switch (getActiveTab.getText()) {
			case "Files":
				webDriver.findElement(By.xpath(String.format(projectLink, "notebooks", projectName))).click();
				break;
			case "Running":
				webDriver.findElement(By.xpath(String.format(projectLink, "running", projectName))).click();
				break;
			case "Clusters":
				webDriver.findElement(By.xpath(String.format(projectLink, "clusters", projectName))).click();
				break;
			default:
				log.error("The project was not found in Tree");
				break;
		}
		return this;
	}

}
