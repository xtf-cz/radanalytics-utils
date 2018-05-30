package cz.xtf.radanalytics.notebook.jupyter.page.objects;

import cz.xtf.radanalytics.waiters.WebWaiters;
import cz.xtf.radanalytics.web.extended.elements.elements.Button;
import cz.xtf.radanalytics.web.extended.elements.elements.DropDownMenu;
import cz.xtf.radanalytics.web.extended.elements.elements.SelectElement;
import cz.xtf.radanalytics.web.extended.elements.elements.TextField;
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
public class ProjectPage extends AbstractPage {
	@FindBy(xpath = "//i[contains(@class, \"fa-step-forward\")]/..")
	private Button runButton;

	@FindBy(xpath = "//select[@id='cell_type']")
	private SelectElement cellTypeDropDown;

	@FindBy(xpath = "//a[@class=\"dropdown-toggle\" and contains(text(), \"Edit\")]")
	private Button editTab;

	@FindBy(xpath = "//ul[@id=\"edit_menu\"]")
	private DropDownMenu dropDownMenu;

	//<editor-fold desc="Find And Replace Modal">
	@FindBy(xpath = "//form[@id=\"find-and-replace\"]")
	private WebElement findAndReplaceModal;

	@FindBy(xpath = "//input[contains(@class, \"form-control\") and @placeholder=\"Find\"]")
	private TextField findAndReplaceModalFindField;

	@FindBy(xpath = "//input[contains(@class, \"form-control\") and @placeholder=\"Replace\"]")
	private TextField findAndReplaceModalReplaceField;

	@FindBy(xpath = "//button[@data-dismiss=\"modal\" and contains(text(), \"Replace All\")]")
	private Button replaceAllButton;
	//</editor-fold>

	private By inputPromptValue = By.xpath(".//div[contains(@class, \"input_prompt\")]");
	private By outPutArea = By.xpath(".//div[@class=\"output\"]");
	private By outPutAreas = By.xpath(".//div[@class=\"output_area\"]");
	private By outPutValue = By.xpath(".//div[contains(@class, \"output_text\")]/pre");
	private By codeLine = By.xpath(".//pre[@class=\" CodeMirror-line \"]");
	private String inputPrompt = "//div[@id=\"notebook-container\"]//div[contains(@class, \"code_cell\")]";


	public ProjectPage(WebDriver webDriver, boolean navigateToPage) {
		super(webDriver, "", navigateToPage, "");
	}

	public ProjectPage(WebDriver webDriver, String hostname, boolean navigateToPage, String projectName) {
		super(webDriver, hostname, navigateToPage, "http://" + hostname + "/notebooks/" + projectName);
	}

	public List<WebElement> getAllCodeCells() {
		WebWaiters.waitUntilJSReady();
		WebWaiters.waitUntilElementIsPresent(inputPrompt, webDriver);
		return webDriver.findElements(By.xpath(inputPrompt));
	}

	public ProjectPage clickOnCell(WebElement cell) {
		cell.click();
		return this;
	}

	public ProjectPage clickOnRunButton() {
		runButton.click();
		return this;
	}

	public char getInputCodeCellPrompt(WebElement cell) {
		return cell.findElement(inputPromptValue).getText().charAt(4);
	}

	public String getOutputArea(WebElement cell) {
		List<WebElement> outputAreas = cell.findElements(outPutAreas);

		StringBuilder output = new StringBuilder();
		for (WebElement element : outputAreas) {
			By byOutput = outPutValue;
			if (element.findElements(byOutput).size() != 0) {
				output.append(element.findElement(byOutput).getText()).append("\n");
			}
		}

		return output.toString();
	}

	public String getOutputArea(WebElement cell, Long timeout) {
		BooleanSupplier successCondition = () ->
				!cell.findElement(outPutArea).getAttribute("style")
						.contains("display: none;");

		try {
			WebWaiters.waitFor(successCondition, null, 1000L, timeout);
		} catch (InterruptedException | TimeoutException e) {
			log.error(e.getMessage());
		}

		return getOutputArea(cell);
	}

	public WebElement getCodeLineContent(WebElement cell, int lineNumber) {
		List<WebElement> codeLines = cell.findElements(codeLine);
		return codeLines.get(lineNumber - 1);
	}

	public ProjectPage selectCellTypeByValue(String value) {
		cellTypeDropDown.selectByValue(value);
		return this;
	}

	public ProjectPage selectCellTypeByIndex(int index) {
		cellTypeDropDown.selectByIndex(index);
		return this;
	}

	public ProjectPage clickOnEditTab() {
		editTab.click();
		return this;
	}

	public ProjectPage chooseItemInDropDownMenu(String item) {
		dropDownMenu.clickOnItem(item);
		WebWaiters.waitUntilElementIsVisible(findAndReplaceModal, webDriver);
		return this;
	}

	public ProjectPage fillFindFieldInFindAndReplaceModal(String findText) {
		findAndReplaceModalFindField.sendKeys(findText);
		return this;
	}

	public ProjectPage fillReplaceFielsInFindAndReplaceModal(String replaceText) {
		fillFindFieldInFindAndReplaceModal(replaceText);
		return this;
	}

	public ProjectPage clickOnReplaceAllButton() {
		replaceAllButton.click();
		WebWaiters.waitUntilElementIsInvisible(findAndReplaceModal, webDriver);
		return this;
	}
}
