package cz.xtf.radanalytics.oshinko.web.webUtils;

import cz.xtf.webdriver.GhostDriverService;
import cz.xtf.webdriver.WebDriverService;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.remote.RemoteWebDriver;

import java.net.MalformedURLException;
import java.net.URL;

public class WebDriverFactory {
	public WebDriver setUpWebDriver(String browser, int podLocalPort) {
		switch (browser) {
			case "phantom":
				return setupPhantomJSDriver();
			case "headless-chrome":
				return setupRemoteChromeDriver(podLocalPort);
			case "headless-firefox":
				return setupRemoteFirefoxDriver(podLocalPort);
			case "headless-chrome-debug":
				return setupRemoteDebugChromeDriver(podLocalPort);
			default:
				throw new IllegalArgumentException("Choose browser type from 'headless-chrome', 'headless-firefox'");
		}
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
			e.printStackTrace();
			return null;
		}
	}

	// TODO investigate reason unsuccessful connection to VNC port 5900;
	private WebDriver setupRemoteDebugChromeDriver(int podLocalPort) {
		ChromeOptions options = new ChromeOptions();
		options.addArguments("window-size=1200x600");
		try {
			return new RemoteWebDriver(new URL(
					"http://localhost:" + podLocalPort + "/wd/hub"),
					options);
		} catch (MalformedURLException e) {
			e.printStackTrace();
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
			e.printStackTrace();
			return null;
		}
	}
}
