package cz.xtf.radanalytics.notebook.zeppelin.page.object;

import cz.xtf.radanalytics.web.extended.elements.elements.Button;
import cz.xtf.radanalytics.web.extended.elements.elements.TextField;
import org.openqa.selenium.support.FindBy;

public class ZeppelinNotebookPage {

	//Settings paragraph $x('//div[@class="setting ng-scope"]')
	@FindBy(xpath = "//button[contains(@class,\"btn btn-primary\") and contains(text(),\"Save\")]")
	private Button saveSettingsBtn;

	@FindBy(xpath ="//button[contains(@class,\"btn btn-default\") and contains(text(),\"Cancel\")]")
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

	@FindBy(xpath ="//button[@uib-tooltip=\"Clone this note\"]")
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

	
}
