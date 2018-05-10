package cz.xtf.radanalytics.web.extended.elements;

import cz.xtf.radanalytics.web.extended.elements.elements.Container;
import org.openqa.selenium.WebElement;

public interface ContainerFactory {
	<C extends Container> C create(Class<C> containerClass, WebElement wrappedElement);

}
