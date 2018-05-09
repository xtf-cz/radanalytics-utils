package cz.xtf.radanalytics.web.extended.elements.impl;

import cz.xtf.radanalytics.web.extended.elements.elements.SelectElement;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.Select;


public class SelectElementImpl extends AbstractElement implements SelectElement {
	protected SelectElementImpl(WebElement wrappedElement) {
		super(wrappedElement);
	}

	@Override
	public void selectByValue(String value) {
		new Select(wrappedElement).selectByValue(value);
	}

	@Override
	public void selectByIndex(int index) {
		new Select(wrappedElement).selectByIndex(index);
	}

	@Override
	public void selectByVisibleText(String text) {
		new Select(wrappedElement).selectByVisibleText(text);
	}
}
