package cz.xtf.radanalytics.oshinko.web;

import cz.xtf.openshift.OpenShiftUtil;
import cz.xtf.openshift.OpenShiftUtils;
import cz.xtf.radanalytics.oshinko.web.webUtils.LocalWebDriverManager;
import cz.xtf.radanalytics.oshinko.web.webUtils.WebDriverFactory;
import cz.xtf.radanalytics.oshinko.web.webUtils.WebDriverPodBuilder;
import org.openqa.selenium.WebDriver;

public abstract class _WebDriver {
	private static final OpenShiftUtil openshift = OpenShiftUtils.master();
	public WebDriver webDriver;

	_WebDriver(){
		String browserName = "headless-chrome";
		new WebDriverPodBuilder(browserName);
		webDriver = webDriverInstance(browserName);
	}

	/**
	 * Choose browser from: headless-chrome, headless-firefox, headless-chrome-debug
	 * @param browserName
	 */
	_WebDriver(String browserName){
		new WebDriverPodBuilder(browserName);
		webDriver = webDriverInstance(browserName);
	}

	private WebDriver webDriverInstance(String browserName) {
		LocalWebDriverManager localWebDriverManager = new LocalWebDriverManager();
		WebDriverFactory webDriverFactory = new WebDriverFactory();
		localWebDriverManager.setWebDriver(webDriverFactory.setUpWebDriver(browserName,
				openshift.client().pods().withName(browserName).portForward(4444).getLocalPort()));
		return localWebDriverManager.getWebDriver();
	}
}
