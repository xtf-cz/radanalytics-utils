package cz.xtf.radanalytics.notebook.sparknotebook.page.objects;

import cz.xtf.radanalytics.waiters.WebWaiters;
import cz.xtf.radanalytics.web.WebHelpers;
import cz.xtf.radanalytics.web.page.objects.AbstractPage;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

import java.util.List;
import java.util.concurrent.TimeoutException;
import java.util.function.BooleanSupplier;

@Slf4j
public class ApplicationPage extends AbstractPage {

	@FindBy(xpath = "//div[@id=\"notebook-container\"]")
	private WebElement notebookConteiner;

	private By dropdownToggle = By.cssSelector("div.cell-context-buttons > div > a.btn.dropdown-toggle");
	private By dropDownMenu = By.xpath(".//ul[contains(@class, \"dropdown-menu-right\")]");
	private By subMenuClear = By.xpath(".//li[@data-menu-command='clear_current_output']");
	private By runButton = By.cssSelector("div.cell-context-buttons > div > a:nth-child(1)");
	private By cancelButtonElement = By.cssSelector("div.progress-bar > a.cancel-cell-btn");
	private By outputAreaField = By.className("output_area");
	private By byOutput = By.xpath(".//div[contains(@class, \"output_subarea output_text\")]/pre");
	private By byTimeOutput = By.cssSelector("small");
	private String inputPrompt = "//div[@id=\"notebook-container\"]//div[contains(@class, \"code_cell\")]";

	public ApplicationPage(WebDriver webDriver, boolean navigateToPage) {
		super(webDriver, "", navigateToPage, "");
	}

	public ApplicationPage(WebDriver webDriver, String hostname, boolean navigateToPage, String projectName) {
		super(webDriver, hostname, navigateToPage, "http://" + hostname + "/notebooks/" + projectName + ".snb.ipynb");
		WebWaiters.waitUntilElementIsVisible(notebookConteiner, webDriver);
	}

	public ApplicationPage scrollToCell(WebElement cell) {
		try {
			WebHelpers.scrollToElement(webDriver, cell);
			WebHelpers.scrollWindowBy(webDriver, -200);
		} catch (InterruptedException e) {
			log.error(e.getMessage());
		}
		return this;
	}

	public ApplicationPage clickOnDropDownMenuToggle(WebElement cell) {
		cell.findElement(dropdownToggle).click();
		return this;
	}

	public ApplicationPage chooseClearCurrentOutput(WebElement cell) {
		cell.findElement(dropDownMenu).findElement(subMenuClear).click();
		return this;
	}

	public ApplicationPage clickOnRunCodeButton(WebElement cell) {
		cell.findElement(runButton).click();
		return this;
	}

	public ApplicationPage isExecutionComlete(WebElement cell) {
		// Execution is complete when progress bar's cancel button goes hidden
		BooleanSupplier successCondition = () -> {
			WebElement cancelButton = cell.findElement(cancelButtonElement);
			return cancelButton.getAttribute("style").equals("display: none;");
		};

		try {
			WebWaiters.waitFor(successCondition, null, 50000L, 900000L);
		} catch (InterruptedException | TimeoutException e) {
			log.error(e.getMessage());
		}
		return this;
	}

	public String getOutputFromCell(WebElement cell) {
		List<WebElement> outputAreas = cell.findElements(outputAreaField);
		StringBuilder output = new StringBuilder();
		for (WebElement element : outputAreas) {

			if (element.findElements(byOutput).size() != 0) {
				output.append(element.findElement(byOutput).getText()).append("\n");
			}
			if (element.findElements(byTimeOutput).size() != 0) {
				output.append(element.findElement(byTimeOutput).getText()).append("\n");
			}
		}
		return output.toString();
	}

	public List<WebElement> getAllCodeCells() {
		WebWaiters.waitUntilJSReady();
		WebWaiters.waitUntilElementIsPresent(inputPrompt, webDriver);
		return webDriver.findElements(By.xpath(inputPrompt));
	}
}
