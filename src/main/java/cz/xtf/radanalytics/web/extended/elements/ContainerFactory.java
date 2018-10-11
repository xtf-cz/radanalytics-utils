package cz.xtf.radanalytics.web.extended.elements;

import org.openqa.selenium.WebElement;

import cz.xtf.radanalytics.web.extended.elements.elements.Container;

public interface ContainerFactory {
	<C extends Container> C create(Class<C> containerClass, WebElement wrappedElement);
}
