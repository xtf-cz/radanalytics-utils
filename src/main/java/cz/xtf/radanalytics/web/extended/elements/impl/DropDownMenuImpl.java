package cz.xtf.radanalytics.web.extended.elements.impl;

import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;

import cz.xtf.radanalytics.web.extended.elements.elements.DropDownMenu;

class DropDownMenuImpl extends AbstractElement implements DropDownMenu {
	protected DropDownMenuImpl(WebElement wrappedElement) {
		super(wrappedElement);
	}

	@Override
	public void clickOnItem(String item) {
		wrappedElement.findElement(By.xpath(String.format(".//a[contains(text(), \"%s\")]", item))).click();
	}
}
