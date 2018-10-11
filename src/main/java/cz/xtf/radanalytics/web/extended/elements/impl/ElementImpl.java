package cz.xtf.radanalytics.web.extended.elements.impl;

import org.openqa.selenium.WebElement;

import cz.xtf.radanalytics.web.extended.elements.elements.Element;

public class ElementImpl extends AbstractElement implements Element {
	protected ElementImpl(WebElement wrappedElement) {
		super(wrappedElement);
	}
}
