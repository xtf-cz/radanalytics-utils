package cz.xtf.radanalytics.web.webdriver;

import cz.xtf.radanalytics.util.configuration.RadanalyticsConfiguration;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.remote.RemoteWebDriver;

import java.net.MalformedURLException;
import java.net.URL;

import cz.xtf.openshift.OpenShiftUtil;
import cz.xtf.openshift.OpenShiftUtils;
import cz.xtf.webdriver.GhostDriverService;
import cz.xtf.webdriver.WebDriverService;

@Slf4j
public class LocalWebDriverManager {

	private static final OpenShiftUtil openshift = OpenShiftUtils.master();

	private static WebDriver chromeWebDriver;
	private static WebDriverPodBuilder chromePod;

	private static WebDriver firefoxWebDriver;
	private static WebDriverPodBuilder firefoxPod;

	private static WebDriver phantomJsWebDriver;

	public static synchronized WebDriver getWebDriver() {
		return getWebDriver(RadanalyticsConfiguration.getBrowserName());
	}

	public static WebDriver getWebDriver(String browser) {

		String browserName;

		switch (browser) {
			default:
			case "chrome":
				browserName = "headless-chrome";
				if (chromePod == null) {
					chromePod = new WebDriverPodBuilder(browserName);
				}

				if (chromeWebDriver == null) {
					chromeWebDriver = setupRemoteChromeDriver(getLocalPortForBrowserInPod(browserName));
				}
				return chromeWebDriver;

			case "firefox":
				browserName = "headless-firefox";
				if (firefoxPod == null) {
					firefoxPod = new WebDriverPodBuilder(browserName);
				}

				if (firefoxWebDriver == null) {
					firefoxWebDriver = setupRemoteFirefoxDriver(getLocalPortForBrowserInPod(browserName));
				}
				return firefoxWebDriver;

			case "phantomjs":
				if (phantomJsWebDriver == null) {
					phantomJsWebDriver = setupPhantomJSDriver();
				}
				return phantomJsWebDriver;
		}
	}

	private static WebDriver setupPhantomJSDriver() {
		GhostDriverService.get().start();
		return WebDriverService.get().start();
	}

	private static WebDriver setupRemoteChromeDriver(int podLocalPort) {
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

	private static WebDriver setupRemoteFirefoxDriver(int podLocalPort) {
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
