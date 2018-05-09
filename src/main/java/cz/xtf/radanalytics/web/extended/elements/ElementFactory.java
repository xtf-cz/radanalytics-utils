package cz.xtf.radanalytics.web.extended.elements;

import cz.xtf.radanalytics.web.extended.elements.elements.Element;
import org.openqa.selenium.WebElement;

public interface ElementFactory {
	<E extends Element> E create(Class<E> elementClass, WebElement wrappedElement);

}
