package cz.xtf.radanalytics.web.webdriver;

import cz.xtf.openshift.OpenShiftUtil;
import cz.xtf.openshift.OpenShiftUtils;
import cz.xtf.webdriver.GhostDriverService;
import cz.xtf.webdriver.WebDriverService;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.remote.RemoteWebDriver;

import java.net.MalformedURLException;
import java.net.URL;

@Slf4j
public class LocalWebDriverManager {
	private static LocalWebDriverManager localWebDriverManager;

	private static final OpenShiftUtil openshift = OpenShiftUtils.master();
	private static WebDriverPodBuilder chromePod;
	private static WebDriverPodBuilder firefoxPod;
	private ThreadLocal<WebDriver> webDriver = new ThreadLocal();

	public WebDriver getWebDriver() {
		return webDriver.get();
	}

	public void setWebDriver(WebDriverBrowser browser) {

		String browserName;

		switch (browser) {
			default:
			case HEADLESS_CHROME:
				browserName = "headless-chrome";
				if (chromePod == null) {
					chromePod = new WebDriverPodBuilder(browserName);
				}

				if (webDriver.get() == null) {
					webDriver.set(setupRemoteChromeDriver(getLocalPortForBrowserInPod(browserName)));
				}
				break;

			case HEADLESS_FIREFOX:
				browserName = "headless-firefox";
				if (firefoxPod == null) {
					firefoxPod = new WebDriverPodBuilder(browserName);
				}

				if (webDriver.get() == null) {
					webDriver.set(setupRemoteFirefoxDriver(getLocalPortForBrowserInPod(browserName)));
				}
				break;

			case PHANTOMJS:
				if (webDriver.get() == null) {
					webDriver.set(setupPhantomJSDriver());
				}
				break;
		}
	}

	public static synchronized LocalWebDriverManager get() {
		if (localWebDriverManager == null) {
			localWebDriverManager = new LocalWebDriverManager();
		}

		return localWebDriverManager;
	}

	private WebDriver setupPhantomJSDriver() {
		GhostDriverService.get().start();
		return WebDriverService.get().start();
	}

	private WebDriver setupRemoteChromeDriver(int podLocalPort) {
		ChromeOptions options = new ChromeOptions();
		options.addArguments("window-size=1200x600");
		try {
			return new RemoteWebDriver(new URL(
					"http://localhost:" + podLocalPort + "/wd/hub"),
					options);
		} catch (MalformedURLException e) {
			log.error(e.getMessage());
			return null;
		}
	}

	private WebDriver setupRemoteFirefoxDriver(int podLocalPort) {
		FirefoxOptions options = new FirefoxOptions();
		options.addPreference("network.proxy.type", 0);
		try {
			return new RemoteWebDriver(new URL(
					"http://localhost:" + podLocalPort + "/wd/hub"),
					options);
		} catch (MalformedURLException e) {
			log.error(e.getMessage());
			return null;
		}
	}

	private static int getLocalPortForBrowserInPod(String deploymentConfigName) {
		String podName = openshift.getAnyPod(deploymentConfigName).getMetadata().getName();
		return openshift.client().pods().withName(podName).portForward(4444).getLocalPort();
	}
}
