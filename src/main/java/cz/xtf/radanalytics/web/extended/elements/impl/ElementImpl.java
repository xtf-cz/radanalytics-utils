package cz.xtf.radanalytics.web.extended.elements.impl;

import cz.xtf.radanalytics.web.extended.elements.elements.Element;
import org.openqa.selenium.WebElement;

public class ElementImpl extends AbstractElement implements Element {
	protected ElementImpl(WebElement wrappedElement) {
		super(wrappedElement);
	}
}
