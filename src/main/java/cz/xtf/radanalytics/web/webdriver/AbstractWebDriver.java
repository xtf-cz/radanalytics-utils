package cz.xtf.radanalytics.web.webdriver;

import cz.xtf.openshift.OpenShiftUtil;
import cz.xtf.openshift.OpenShiftUtils;
import org.openqa.selenium.WebDriver;

public abstract class AbstractWebDriver {
	private static final OpenShiftUtil openshift = OpenShiftUtils.master();
	public WebDriver webDriver;
	private static WebDriverPodBuilder webDriverPod;
	private static LocalWebDriverManager localWebDriverManager;
	private static WebDriverFactory webDriverFactory;

	protected AbstractWebDriver(){
		initAbstractWebDriver("headless-chrome");
	}

	/**
	 * Choose browser from: headless-chrome, headless-firefox, headless-chrome-debug
	 * @param browserName
	 */
	protected AbstractWebDriver(String browserName) {
		initAbstractWebDriver(browserName);
	}

	private void initAbstractWebDriver(String browserName) {
		if (webDriverPod == null){
			webDriverPod = new WebDriverPodBuilder(browserName);
		}
		webDriver = webDriverInstance(browserName);
	}

	private WebDriver webDriverInstance(String browserName) {
		if (localWebDriverManager == null) {
			localWebDriverManager = new LocalWebDriverManager();
			webDriverFactory = new WebDriverFactory();
		}
		if (localWebDriverManager.getWebDriver() == null){
			localWebDriverManager.setWebDriver(webDriverFactory.setUpWebDriver(browserName,
					openshift.client().pods().withName(browserName).portForward(4444).getLocalPort()));
		}
		return localWebDriverManager.getWebDriver();
	}
}
