package cz.xtf.radanalytics.web.extended.elements.impl;

import org.openqa.selenium.WebElement;

import cz.xtf.radanalytics.web.extended.elements.ContainerFactory;
import cz.xtf.radanalytics.web.extended.elements.elements.Container;

public class DefaultContainerFactory implements ContainerFactory {
	@Override
	public <C extends Container> C create(final Class<C> containerClass, final WebElement wrappedElement) {
		final C container = createInstanceOf(containerClass);
		container.init(wrappedElement);
		return container;
	}

	private <C extends Container> C createInstanceOf(final Class<C> containerClass) {
		try {
			return containerClass.newInstance();
		} catch (InstantiationException | IllegalAccessException e) {
			throw new RuntimeException(e);
		}
	}
}
