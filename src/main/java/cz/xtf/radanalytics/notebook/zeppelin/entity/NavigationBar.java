package cz.xtf.radanalytics.notebook.zeppelin.entity;

import cz.xtf.radanalytics.web.extended.elements.elements.Button;
import cz.xtf.radanalytics.web.extended.elements.elements.DropDownMenu;
import cz.xtf.radanalytics.web.extended.elements.elements.TextField;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.support.FindBy;

@Slf4j
public class NavigationBar {

	@FindBy(xpath = "//div[@class=\"navbar-header\"]")
	private Button zeppelinLogoButton;

	@FindBy(xpath = "//li[@class=\"dropdown notebook-list-dropdown open\"]")
	private DropDownMenu dropDownMenu;

	@FindBy(xpath = "//span[text()=\"Job\"]")
	private Button jobButton;

	@FindBy(xpath = "//*[text()=\" Create new note \"]")
	private Button addNewNote;

	@FindBy(xpath = "placeholder=\"\uF002 Filter\"")
	private TextField notebookFilter;


}
