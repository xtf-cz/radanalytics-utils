package cz.xtf.radanalytics.notebook.zeppelin.page.object;

import cz.xtf.radanalytics.waiters.WebWaiters;
import cz.xtf.radanalytics.web.extended.elements.elements.Button;
import cz.xtf.radanalytics.web.extended.elements.elements.TextField;
import cz.xtf.radanalytics.web.page.objects.AbstractPage;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;

public class ZeppelinNotebookPage extends AbstractPage {

	private final String paragraphsXpath = "//div[contains(@class,\"paragraph-col\")]";

	//Settings paragraph $x('//div[@class="setting ng-scope"]')
	@FindBy(xpath = "//button[contains(@class,\"btn btn-primary\") and contains(text(),\"Save\")]")
	private Button saveSettingsBtn;
	@FindBy(xpath = "//button[contains(@class,\"btn btn-default\") and contains(text(),\"Cancel\")]")
	private Button cancelSettingsBtn;

	//Notebook toolbar $x('//headroom[contains(@class,"headroom")]')
	@FindBy(xpath = "//button[@uib-tooltip=\"Run all paragraphs\"]")
	private Button runAllParagraphsBtn;

	@FindBy(xpath = "//button[@uib-tooltip=\"Show/hide the code\"]")
	private Button showHideCodeBtn;

	@FindBy(xpath = "//button[@uib-tooltip=\"Show/hide the output\"]")
	private Button showHideOutputBtn;

	@FindBy(xpath = "//button[@uib-tooltip=\"Clear output\"]")
	private Button clearOutputBtn;

	@FindBy(xpath = "//button[@uib-tooltip=\"Clone this note\"]")
	private Button cloneThisNote;

	@FindBy(xpath = "//button[@uib-tooltip=\"Export this note\"]")
	private Button exportNoteBtn;

	@FindBy(id = "versionControlDropdown")
	private Button versionControlBtn;

	@FindBy(id = "note.checkpoint.message")
	private TextField commitMessageTextField;

	@FindBy(xpath = "//button[contains(text(),\"Commit\")]")
	private Button commitBtn;

	@FindBy(xpath = "//button[@uib-tooltip=\"Compare revisions\"]")
	private Button compareRevisionBtn;

	@FindBy(id = "revisionsDropdown")
	private Button revisionDDl;

	private final String revisionDDlxpath = "//*[contains(@class,\"dropdown-menu\") and contains(@aria-labelledby,\"revisionsDropdown\")]";


	//Run all paragraphs modal pup-up $x('//*[text()="Run all paragraphs?"]/../../../..')

	@FindBy(xpath = "//div[contains(text(),\"Run all paragraphs?\")]")
	private WebElement confirmRunAllParagraphsPopup;

//div[contains(@class,"plainTextContent")]

	@FindBy(xpath = "//button[text()=\"OK\"]")
	private Button confirmRunAllParagraphs;

	@FindBy(xpath = "//button[text()=\"Cancel\"]")
	private Button cancelRunAllParagraphs;

	public ZeppelinNotebookPage(WebDriver webDriver, boolean navigateToPage) {
		super(webDriver, "", navigateToPage, "");
	}

	public ZeppelinNotebookPage(WebDriver webDriver, String hostname, boolean navigateToPage, String projectName) {
		super(webDriver, hostname, navigateToPage, new StringBuilder().append("http://").append(hostname).append("/notebooks/").append(projectName).toString());
	}

	public boolean areParagraphsPresent() {
		WebWaiters.waitUntilJSReady();
		WebWaiters.waitUntilElementIsPresent(paragraphsXpath, webDriver);
		return !webDriver.findElements(By.xpath(paragraphsXpath)).isEmpty();
	}

	public ZeppelinNotebookPage saveSettings() {
		saveSettingsBtn.click();
		return this;
	}

	public ZeppelinNotebookPage cancelSettings() {
		cancelSettingsBtn.click();
		return this;
	}

	public ZeppelinNotebookPage runAllParagraphs() {
		runAllParagraphsBtn.click();
		return this;
	}

	public ZeppelinNotebookPage confirmRunAllParagraphs() {
		WebWaiters.waitForAngularLoad();
		WebWaiters.waitUntilElementIsVisible(confirmRunAllParagraphsPopup, webDriver);
		confirmRunAllParagraphs.click();
		return this;
	}

	public ZeppelinNotebookPage pressShowHideCodeBtn() {
		showHideCodeBtn.click();
		return this;
	}

	public ZeppelinNotebookPage pressShowHideOutputsBtn() {
		showHideOutputBtn.click();
		return this;
	}

	public ZeppelinNotebookPage pressClearOutputBtn() {
		clearOutputBtn.click();
		return this;
	}

	public ZeppelinNotebookPage pressCloneThisNote() {
		cloneThisNote.click();
		return this;
	}

	public ZeppelinNotebookPage pressExportNoteBtn() {
		exportNoteBtn.click();
		return this;
	}

	public ZeppelinNotebookPage pressVersionControlBtn() {
		versionControlBtn.click();
		return this;
	}

	public ZeppelinNotebookPage addCommitMessage(String commitMessage) {
		commitMessageTextField.clear();
		commitMessageTextField.sendKeys(commitMessage);
		return this;
	}

	public ZeppelinNotebookPage commit() {
		commitBtn.click();
		return this;
	}

	public ZeppelinNotebookPage openRevisionDDl() {
		revisionDDl.click();
		WebWaiters.waitUntilElementIsPresent(revisionDDlxpath, webDriver);
		return this;
	}


}
