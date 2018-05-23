package cz.xtf.radanalytics.web.extended.elements.impl;

import org.openqa.selenium.WebElement;

import cz.xtf.radanalytics.web.extended.elements.elements.Element;

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
	public String getAttribute(String attribute) {
		return wrappedElement.getAttribute(attribute);
	}
}
