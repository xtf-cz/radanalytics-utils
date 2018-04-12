package cz.xtf.radanalytics.webUtils;

import cz.xtf.openshift.OpenShiftUtil;
import cz.xtf.openshift.OpenShiftUtils;
import org.openqa.selenium.WebDriver;

public abstract class AbstractWebDriver {
	private static final OpenShiftUtil openshift = OpenShiftUtils.master();
	public WebDriver webDriver;

	protected AbstractWebDriver(){
		String browserName = "headless-chrome";
		new WebDriverPodBuilder(browserName);
		webDriver = webDriverInstance(browserName);
	}

	/**
	 * Choose browser from: headless-chrome, headless-firefox, headless-chrome-debug
	 * @param browserName
	 */
	protected AbstractWebDriver(String browserName){
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
