package cz.xtf.radanalytics.web.extended.elements.impl;

import org.openqa.selenium.WebElement;

import cz.xtf.radanalytics.web.extended.elements.elements.Button;

class ButtonImpl extends AbstractElement implements Button {
	protected ButtonImpl(final WebElement wrappedElement) {
		super(wrappedElement);
	}

	@Override
	public void click() {
		wrappedElement.click();
	}
}
