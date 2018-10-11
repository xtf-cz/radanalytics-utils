package cz.xtf.radanalytics.notebook.zeppelin.page.object;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import java.util.List;
import java.util.stream.Collectors;

import cz.xtf.radanalytics.util.junit5.annotation.WebUITests;
import cz.xtf.radanalytics.waiters.WebWaiters;
import cz.xtf.radanalytics.web.extended.elements.elements.DropDownMenu;
import cz.xtf.radanalytics.web.extended.elements.elements.TextField;
import cz.xtf.radanalytics.web.page.objects.AbstractPage;

@WebUITests
public class ZeppelinJobPage extends AbstractPage {

	private final String jobsXpath = "//div[contains(@class,\"paragraph-col\")]";

	@FindBy(xpath = "//div[contains(@class,\"search-input\")]")
	private TextField searchJobsField;
	@FindBy(xpath = "//div[@class=\"btn btn-default dropdown-toggle\"]")
	private DropDownMenu interpreterDDl;
	@FindBy(xpath = "//div[contains(@class,\"date-sort-button\")]")
	private DropDownMenu sortDDl;
	@FindBy(xpath = "//span[contains(@class,\"job-counter-value\")]")
	private WebElement totalJobsOnPage;

	public ZeppelinJobPage(WebDriver webDriver, String hostname, boolean navigateToPage, String navigateToPageUrl) {
		super(webDriver, hostname, navigateToPage,
				new StringBuilder().append("http://").append(navigateToPageUrl).append("/#/jobmanager").toString());
	}

	public ZeppelinJobPage searchJob(String text) {
		searchJobsField.sendKeys(text);
		WebWaiters.waitForAngularLoad();
		return this;
	}

	public ZeppelinJobPage selectInterpreter(String interpreterName) {
		interpreterDDl.clickOnItem(interpreterName);
		WebWaiters.waitForAngularLoad();
		return this;
	}

	public ZeppelinJobPage selectJobsSorting(String sortingType) {
		sortDDl.clickOnItem(sortingType);
		WebWaiters.waitForAngularLoad();
		return this;
	}

	public ZeppelinJobPage sortJobRecentlyUpdated() {
		selectJobsSorting("Recently Update ");
		return this;
	}

	public ZeppelinJobPage sortJobOldestUpdated() {
		selectJobsSorting("Oldest Updated ");
		return this;
	}

	public List<WebElement> getAllJobs() {
		WebWaiters.waitForAngularLoad();
		return webDriver.findElements(By.xpath(jobsXpath));
	}

	public List<WebElement> findByName(String jobName) {
		return getAllJobs().stream()
				.filter(job -> job.getText().contains(jobName))
				.collect(Collectors.toList());
	}

	public Long getTotalJobAmount() {
		WebWaiters.waitForAngularLoad();
		return Long.parseLong(totalJobsOnPage.getText());
	}
}
