package cz.xtf.radanalytics.web.extended.elements.impl;

import cz.xtf.radanalytics.web.extended.elements.elements.TextField;
import org.openqa.selenium.WebElement;

class TextFieldImpl extends AbstractElement implements TextField {
	protected TextFieldImpl(final WebElement wrappedElement) {
		super(wrappedElement);
	}

	@Override
	public void type(final String text) {
		wrappedElement.sendKeys(text);
	}

	@Override
	public void clear() {
		wrappedElement.clear();
	}

	@Override
	public void clearAndType(final String text) {
		clear();
		type(text);
	}

	@Override
	public void sendKeys(String text) {
		wrappedElement.sendKeys(text);
	}
}
