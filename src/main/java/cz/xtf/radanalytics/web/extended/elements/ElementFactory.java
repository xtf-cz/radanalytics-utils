package cz.xtf.radanalytics.web.extended.elements;

import org.openqa.selenium.WebElement;

import cz.xtf.radanalytics.web.extended.elements.elements.Element;

public interface ElementFactory {
	<E extends Element> E create(Class<E> elementClass, WebElement wrappedElement);
}
