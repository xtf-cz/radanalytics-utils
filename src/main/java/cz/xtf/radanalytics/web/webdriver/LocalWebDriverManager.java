package cz.xtf.radanalytics.web.webdriver;

import cz.xtf.radanalytics.util.configuration.RadanalyticsConfiguration;
import cz.xtf.radanalytics.waiters.WebWaiters;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.remote.RemoteWebDriver;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.BooleanSupplier;

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
		return getWebDriver(RadanalyticsConfiguration.getWebDriverBrowserName());
	}

	public static WebDriver getWebDriver(String browser) {

		String browserName;

		switch (browser) {
			default:
			case "chrome":
				browserName = "headless-chrome";
				if (chromePod == null) {
					log.info("Creating WebDriverPodBuilder");
					chromePod = new WebDriverPodBuilder(browserName);
				}

				if (chromeWebDriver == null) {
					log.info("Setting up RemoteChromeDriver");
					chromeWebDriver = setupRemoteChromeDriver(getLocalPortForBrowserInPod(browserName));
				}
				chromeWebDriver.manage().timeouts().pageLoadTimeout(60, TimeUnit.SECONDS);
				return chromeWebDriver;

			case "firefox":
				browserName = "headless-firefox";
				if (firefoxPod == null) {
					firefoxPod = new WebDriverPodBuilder(browserName);
				}

				if (firefoxWebDriver == null) {
					firefoxWebDriver = setupRemoteFirefoxDriver(getLocalPortForBrowserInPod(browserName));
				}
				firefoxWebDriver.manage().timeouts().pageLoadTimeout(60, TimeUnit.SECONDS);
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
		String gridUrl = "http://localhost:" + podLocalPort + "/wd/hub";
		ChromeOptions options = new ChromeOptions();
		options.addArguments("window-size=1200x600");
		try {
			isRemoteHostReady(gridUrl);
		} catch (TimeoutException | InterruptedException e) {
			log.error("Remote host of grid WebDriver is not ready {}", e.getMessage());
		}
		try {
			log.info("Creating instance of RemoteWebDriver");
			return new RemoteWebDriver(new URL(
					gridUrl),
					options);
		} catch (MalformedURLException e) {
			log.error(e.getMessage());
			return null;
		}
	}

	private static WebDriver setupRemoteFirefoxDriver(int podLocalPort) {
		String gridUrl = "http://localhost:" + podLocalPort + "/wd/hub";
		FirefoxOptions options = new FirefoxOptions();
		options.addPreference("network.proxy.type", 0);
		try {
			isRemoteHostReady(gridUrl);
		} catch (TimeoutException | InterruptedException e) {
			log.error("Remote host of grid WebDriver is not ready {}", e.getMessage());
		}
		try {
			return new RemoteWebDriver(new URL(
					gridUrl),
					options);
		} catch (MalformedURLException e) {
			log.error(e.getMessage());
			return null;
		}
	}

	private static int getLocalPortForBrowserInPod(String deploymentConfigName) {
		String podName = openshift.getAnyPod(deploymentConfigName).getMetadata().getName();
		int localPort = openshift.client().pods().withName(podName).portForward(4444).getLocalPort();
		log.info("Local port for remote webDriver {}", localPort);
		return localPort;
	}

	private static boolean isRemoteHostReady(String gridUrl) throws TimeoutException, InterruptedException {
		log.debug("Trying to get response code from {}", gridUrl);
		URL link = null;
		try {
			link = new URL(gridUrl);
		} catch (IOException e) {
			log.error("The following url provided is malformed: {}", gridUrl);
			log.error(e.getMessage());
		}
		URL finalLink = link;
		BooleanSupplier successConditionForConnection = () -> {
			try {
				HttpURLConnection connection;
				connection = (HttpURLConnection) finalLink.openConnection();
				connection.setRequestMethod("GET");
				connection.connect();
				log.debug("Code response of grid webDriver is {}", connection.getResponseCode());
				return connection.getResponseCode() == 200;
			} catch (IOException e) {
				log.error("Request for response code is failed {}", e.getMessage());
				return false;
			}
		};
		return WebWaiters.waitFor(successConditionForConnection, null, 1000L, 60 * 1000L);
	}
}
