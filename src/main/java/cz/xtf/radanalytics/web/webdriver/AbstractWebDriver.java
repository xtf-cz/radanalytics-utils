package cz.xtf.radanalytics.web.webdriver;

import org.openqa.selenium.WebDriver;

public abstract class AbstractWebDriver {
	public WebDriver webDriver;

	protected AbstractWebDriver() {
		this(WebDriverBrowser.HEADLESS_CHROME);
	}

	/**
	 * Choose browser from: headless-chrome, headless-firefox
	 *
	 * @param browser
	 */
	protected AbstractWebDriver(WebDriverBrowser browser) {
		LocalWebDriverManager.get().setWebDriver(browser);
		webDriver = LocalWebDriverManager.get().getWebDriver();
	}
}
