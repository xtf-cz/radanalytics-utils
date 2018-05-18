package cz.xtf.radanalytics.web.webdriver;

import org.openqa.selenium.WebDriver;

public abstract class AbstractWebDriver {
	public WebDriver webDriver;

	protected AbstractWebDriver() {
		webDriver = LocalWebDriverManager.getWebDriver();
	}
}
