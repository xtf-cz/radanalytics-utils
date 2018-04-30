package cz.xtf.radanalytics.waiters;

import static org.openqa.selenium.support.ui.ExpectedConditions.presenceOfElementLocated;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

public class WebWaiters {

	public static void waitUntilElementIsVisible(WebElement webElement, WebDriver webDriver) {
		new WebDriverWait(webDriver, 30).until(ExpectedConditions.visibilityOf(webElement));
	}

	public static void waitUntilElementIsPresent(String xpath, WebDriver webDriver, int timeOutInSeconds) {
		new WebDriverWait(webDriver, timeOutInSeconds).until(presenceOfElementLocated(By.xpath(xpath)));
	}

	public static void waitUntilElementIsPresent(String xpath, WebDriver webDriver) {
		waitUntilElementIsPresent(xpath, webDriver, 30);
	}

	public static void waitUntilElementIsNotPresent(String xpath, WebDriver webDriver) {
		new WebDriverWait(webDriver, 30).until(ExpectedConditions.numberOfElementsToBe(By.xpath(xpath), 0));
	}
}
