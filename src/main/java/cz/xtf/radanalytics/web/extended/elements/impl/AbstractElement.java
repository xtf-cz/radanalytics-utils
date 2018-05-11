package cz.xtf.radanalytics.web.extended.elements.impl;

import cz.xtf.radanalytics.web.extended.elements.elements.Element;
import org.openqa.selenium.WebElement;

abstract class AbstractElement implements Element {
	protected final WebElement wrappedElement;

	protected AbstractElement(final WebElement wrappedElement) {
		this.wrappedElement = wrappedElement;
	}

	@Override
	public boolean isDisplayed() {
		return wrappedElement.isDisplayed();
	}

	@Override
	public WebElement getElement() {
		return wrappedElement;
	}

	@Override
	public void getAttribute(String attribute) {
		wrappedElement.getAttribute(attribute);
	}
}
