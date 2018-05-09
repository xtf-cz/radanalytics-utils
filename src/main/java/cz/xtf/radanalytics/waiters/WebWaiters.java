package cz.xtf.radanalytics.waiters;

import static java.lang.Thread.sleep;
import static org.openqa.selenium.support.ui.ExpectedConditions.presenceOfElementLocated;

import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.util.concurrent.TimeoutException;
import java.util.function.BooleanSupplier;

@Slf4j
public class WebWaiters {
	private static WebDriver jsWaitDriver;
	private static JavascriptExecutor jsExec;
	private static WebDriverWait jsWait;
	public static final long DEFAULT_WAIT_INTERVAL = 1000L; // one second
	public static final long DEFAULT_WAIT_TIMEOUT = 900000L;

	public static void waitUntilElementIsVisible(WebElement webElement, WebDriver webDriver) {
		new WebDriverWait(webDriver, 30).until(ExpectedConditions.visibilityOf(webElement));
	}

	public static void waitUntilElementIsInvisible(WebElement webElement, WebDriver webDriver) {
		new WebDriverWait(webDriver, 30).until(ExpectedConditions.invisibilityOf(webElement));
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

	public static boolean waitFor(BooleanSupplier condition, BooleanSupplier failCondition, long interval, long timeout) throws InterruptedException, TimeoutException {

		timeout = System.currentTimeMillis() + timeout;

		while (System.currentTimeMillis() < timeout) {

			if (failCondition != null && failCondition.getAsBoolean()) {
				return false;
			}

			if (condition.getAsBoolean()) {
				return true;
			}

			Thread.sleep(interval);
		}

		throw new TimeoutException();
	}

	public static boolean waitFor(BooleanSupplier condition) throws InterruptedException, TimeoutException {
		return waitFor(condition, null, DEFAULT_WAIT_INTERVAL, DEFAULT_WAIT_TIMEOUT);
	}

	//Get the driver
	public static void setDriver (WebDriver driver) {
		jsWaitDriver = driver;
		jsWait = new WebDriverWait(jsWaitDriver, 10);
		jsExec = (JavascriptExecutor) jsWaitDriver;
	}

	//Wait for JQuery Load
	public static void waitForJQueryLoad() {
		//Wait for jQuery to load
		ExpectedCondition<Boolean> jQueryLoad = driver -> ((Long) ((JavascriptExecutor) jsWaitDriver)
				.executeScript("return jQuery.active") == 0);

		//Get JQuery is Ready
		boolean jqueryReady = (Boolean) jsExec.executeScript("return jQuery.active==0");

		//Wait JQuery until it is Ready!
		if(!jqueryReady) {
			log.debug("JQuery is NOT Ready!");
			//Wait for jQuery to load
			jsWait.until(jQueryLoad);
		} else {
			log.debug("JQuery is Ready!");
		}
	}

	//Wait for Angular Load
	public static void waitForAngularLoad() {
		WebDriverWait wait = new WebDriverWait(jsWaitDriver,15);
		JavascriptExecutor jsExec = (JavascriptExecutor) jsWaitDriver;

		String angularReadyScript = "return angular.element(document).injector().get('$http').pendingRequests.length === 0";

		//Wait for ANGULAR to load
		ExpectedCondition<Boolean> angularLoad = driver -> Boolean.valueOf(((JavascriptExecutor) driver)
				.executeScript(angularReadyScript).toString());

		//Get Angular is Ready
		boolean angularReady = Boolean.valueOf(jsExec.executeScript(angularReadyScript).toString());

		//Wait ANGULAR until it is Ready!
		if(!angularReady) {
			log.debug("ANGULAR is NOT Ready!");
			//Wait for Angular to load
			wait.until(angularLoad);
		} else {
			log.debug("ANGULAR is Ready!");
		}
	}

	//Wait Until JS Ready
	public static void waitUntilJSReady() {
		WebDriverWait wait = new WebDriverWait(jsWaitDriver,15);
		JavascriptExecutor jsExec = (JavascriptExecutor) jsWaitDriver;

		//Wait for Javascript to load
		ExpectedCondition<Boolean> jsLoad = driver -> ((JavascriptExecutor) jsWaitDriver)
				.executeScript("return document.readyState").toString().equals("complete");

		//Get JS is Ready
		boolean jsReady =  (Boolean) jsExec.executeScript("return document.readyState").toString().equals("complete");

		//Wait Javascript until it is Ready!
		if(!jsReady) {
			log.debug("JS in NOT Ready!");
			//Wait for Javascript to load
			wait.until(jsLoad);
		}
	}

	//Wait Until JQuery and JS Ready
	public static void waitUntilJQueryReady() throws InterruptedException {
		JavascriptExecutor jsExec = (JavascriptExecutor) jsWaitDriver;

		//First check that JQuery is defined on the page. If it is, then wait AJAX
		Boolean jQueryDefined = (Boolean) jsExec.executeScript("return typeof jQuery != 'undefined'");
		if (jQueryDefined) {
			//Pre Wait for stability (Optional)
			sleep(20);

			//Wait JQuery Load
			waitForJQueryLoad();

			//Wait JS Load
			waitUntilJSReady();

			//Post Wait for stability (Optional)
			sleep(20);
		}  else {
			log.debug("jQuery is not defined on this site!");
		}
	}
}
