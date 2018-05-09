package cz.xtf.radanalytics.web.extended.elements.impl;

import cz.xtf.radanalytics.web.extended.elements.elements.DropDownMenu;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

class DropDownMenuImpl extends AbstractElement implements DropDownMenu {
	protected DropDownMenuImpl(WebElement wrappedElement) {
		super(wrappedElement);
	}

	@Override
	public void clickOnItem(String item) {
		wrappedElement.findElement(By.xpath(String.format(".//a[contains(text(), \"%s\")]", item))).click();
	}
}
