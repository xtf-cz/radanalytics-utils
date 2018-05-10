package cz.xtf.radanalytics.web.extended.elements.impl;

import cz.xtf.radanalytics.web.extended.elements.elements.Button;
import org.openqa.selenium.WebElement;

class ButtonImpl extends AbstractElement implements Button {
	protected ButtonImpl(final WebElement wrappedElement) {
		super(wrappedElement);
	}

	@Override
	public void click() {
		wrappedElement.click();
	}
}
