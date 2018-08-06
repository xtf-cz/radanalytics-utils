package cz.xtf.radanalytics.notebook.sparknotebook.page.objects;

import cz.xtf.radanalytics.util.junit5.annotation.WebUITests;
import cz.xtf.radanalytics.waiters.WebWaiters;
import cz.xtf.radanalytics.web.WebHelpers;
import cz.xtf.radanalytics.web.page.objects.AbstractPage;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

@WebUITests
public class MainPage extends AbstractPage {
	private String projectInList = "//div[@id=\"notebook_list\"]//span[@class=\"item_name\" and text()=\"%s\"]";

	public MainPage(WebDriver webDriver, boolean navigateToPage) {
		super(webDriver, "", navigateToPage, "" );
	}

	public MainPage(WebDriver webDriver, String hostname, boolean navigateToPage) {
		super(webDriver, hostname, navigateToPage, "http://" + hostname + "/notebooks/" );
	}

	public ApplicationPage clickOnProjectInList(String projectName) {
		String projectInListXpath = String.format(projectInList, projectName);
		WebWaiters.waitUntilElementIsPresent(projectInListXpath, webDriver);
		webDriver.findElement(By.xpath(projectInListXpath)).click();
		WebHelpers.switchToLastOpenedTab(webDriver);
		return new ApplicationPage(webDriver, false);
	}
}
