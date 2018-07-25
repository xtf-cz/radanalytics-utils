package cz.xtf.radanalytics.notebook.zeppelin.page.object;

import cz.xtf.radanalytics.waiters.WebWaiters;
import cz.xtf.radanalytics.web.extended.elements.elements.Button;
import cz.xtf.radanalytics.web.extended.elements.elements.DropDownMenu;
import cz.xtf.radanalytics.web.extended.elements.elements.TextField;
import cz.xtf.radanalytics.web.page.objects.AbstractPage;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.FindBy;

public class ZeppelinPage extends AbstractPage {

	private final String notebookDropDownMenuXpath = "//li[@class=\"dropdown notebook-list-dropdown open\"]";
	// Navigation bar $x('//div[@ng-controller="NavCtrl as navbar"]')
	@FindBy(xpath = "//div[@class=\"navbar-header\"]")
	private Button zeppelinLogoButton;
	@FindBy(xpath = "//span[text()=\"Notebook\"]/..")
	private Button notebookMenuBtr;
	@FindBy(xpath = "notebookDropDownMenuXpath")
	private DropDownMenu dropDownMenu;

	@FindBy(xpath = "//span[text()=\"Job\"]")
	private Button jobButton;

	@FindBy(xpath = "//*[text()=\" Create new note \"]")
	private Button addNewNote;

	@FindBy(xpath = "//*[@id=\"notebook-list\"]/li[@class=\"filter-names ng-scope\"]")
	private TextField notebookFilter;

	@FindBy(xpath = "//*[text()=\"Basic Features (Spark)\"]")
	private Button tutorialBasicFeatureSpark;

	@FindBy(xpath = "//*[text()=\"Matplotlib (Python • PySpark)\"]")
	private Button tutorialMatplotlibPySpark;

	@FindBy(xpath = "//*[text()=\"R (SparkR)\"]")
	private Button tutorialSparkR;

	@FindBy(xpath = "//*[text()=\"Using Flink for batch processing\"]")
	private Button tutorialFlinkBatchProcessing;

	@FindBy(xpath = "//*[text()=\"Using Mahout\"]")
	private Button tutorialMahout;

	@FindBy(xpath = "//*[text()=\"Using Pig for querying data\"]")
	private Button tutorialPigQuery;

	@FindBy(xpath = "//a[@href=\"#/notebook/2D6KZ29RA\"]")
	private Button untitledNoteOneNotebook;

	@FindBy(xpath = "//a[@href=\"#/notebook/2D6UCXJ3M\"]")
	private Button armDateNotebook;

	//Zeppelin home controller plate $x('//div[@ng-controller="HomeCtrl as home"]')
	@FindBy(xpath = "//a[@class=\"ng-binding\" and @href=\"#/notebook/2D6UCXJ3M\"]")
	private Button armDateNotebookHomeController;

	@FindBy(xpath = "//a[@class=\"ng-binding\" and @href=\"#/notebook/2D6KZ29RA\"]")
	private Button untitledNoteOneNotebookHomeController;

	@FindBy(xpath = "//*[text()=\" Zeppelin Tutorial \"]")
	private Button zeppelinTutorialHomeController;

	@FindBy(xpath = "//*[text()=\" Import note\"]")
	private Button importNoteHomeController;

	@FindBy(xpath = "//*[text()=\" Create new note\"]")
	private Button createNewNotebookHomeController;


	public ZeppelinPage(WebDriver webDriver, String hostname, boolean navigateToPage, String navigateToPageUrl) {
		super(webDriver, hostname, navigateToPage, new StringBuilder().append("http://").append(navigateToPageUrl).toString());
	}

	public ZeppelinPage pressHomeBtr() {
		zeppelinLogoButton.click();
		return this;
	}

	public ZeppelinPage openNotebookDDl() {
		notebookMenuBtr.click();
		WebWaiters.waitForAngularLoad();
		WebWaiters.waitUntilElementIsPresent(notebookDropDownMenuXpath, webDriver);
		return this;
	}

	public ZeppelinPage pressJobButton() {
		jobButton.click();
		return this;
	}
}
