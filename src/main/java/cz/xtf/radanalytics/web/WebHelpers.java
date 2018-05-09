package cz.xtf.radanalytics.web;

import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import java.util.ArrayList;
import java.util.List;

public class WebHelpers {

	public static void scrollToElement(WebDriver driver, WebElement element) throws InterruptedException {
		((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView(true);", element);
		Thread.sleep(500);
	}

	public static void scrollWindowBy(WebDriver driver, int shiftPixels) throws InterruptedException {
		((JavascriptExecutor) driver).executeScript(String.format("window.scrollBy(0,%s);", shiftPixels));
		Thread.sleep(500);
	}

	public static void switchWindowTab(WebDriver driver, int windowInQueue){
		List<String> listWindows = new ArrayList<>(driver.getWindowHandles());
		driver.switchTo().window(listWindows.get(windowInQueue));
	}
}
