package cz.xtf.radanalytics.web.extended.elements.elements;

public interface SelectElement extends Element {
	void selectByValue(String value);

	void selectByIndex(int index);

	void selectByVisibleText(String text);
}
