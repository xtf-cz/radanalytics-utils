package cz.xtf.radanalytics.web;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

public class WebWaiters {

	public static void waitUntilElementIsVisible(WebElement webElement, WebDriver webDriver) {
		new WebDriverWait(webDriver, 30).until(ExpectedConditions.visibilityOf(webElement));
	}
}
