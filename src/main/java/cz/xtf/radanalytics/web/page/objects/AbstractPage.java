package cz.xtf.radanalytics.web.page.objects;

import cz.xtf.radanalytics.waiters.WebWaiters;
import cz.xtf.radanalytics.web.extended.elements.ExtendedFieldDecorator;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.PageFactory;

import javax.net.ssl.HttpsURLConnection;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.Objects;
import java.util.function.BooleanSupplier;

@Slf4j
public abstract class AbstractPage {
	public final String hostname;
	public final WebDriver webDriver;

	public AbstractPage(WebDriver webDriver, String hostname, boolean navigateToPage, String navigateToPageUrl) {
		this.webDriver = webDriver;
		this.hostname = hostname;
		PageFactory.initElements(new ExtendedFieldDecorator(webDriver), this);
		WebWaiters.setDriver(webDriver);
		if (navigateToPage) {
			if (!Objects.equals(webDriver.getCurrentUrl(), navigateToPageUrl)) {
				pageLoaded(60 * 1000L, navigateToPageUrl, 4);
				webDriver.get(navigateToPageUrl);
			}
		}
	}

	private void pageLoaded(Long interval, String url, int countTimes) {
		log.debug("Trying to get response code from {}", url);
		URL link = null;
		URLConnection conn = null;
		try {
			link = new URL(url);
			conn = link.openConnection();
		} catch (IOException e) {
			log.error("The following url provided is malformed: {}", url);
			log.error(e.getMessage());
		}
		URL finalLink = link;
		URLConnection finalConn = conn;
		BooleanSupplier successConditionForConnection = () -> {
			try {
				HttpURLConnection connection;
				if (finalConn instanceof HttpsURLConnection){
					connection = (HttpsURLConnection) finalLink.openConnection();
				} else {
					connection = (HttpURLConnection) finalLink.openConnection();
				}
				connection.setRequestMethod("GET");
				connection.connect();
				log.debug("URL response is {}", finalLink);
				log.debug("Code response is {}", connection.getResponseCode());
				return connection.getResponseCode() == 200;
			} catch (IOException e) {
				log.error("Request for response code is failed {}", e.getMessage());
				return false;
			}
		};
		BooleanSupplier successConditionForPageReady = () -> ((JavascriptExecutor) webDriver).executeScript("return document.readyState").toString().equals("complete");
		int countTries = 0;
		while (!(successConditionForConnection.getAsBoolean() && successConditionForPageReady.getAsBoolean())) {
			countTries++;
			try {
				Thread.sleep(interval);
			} catch (InterruptedException e) {
				log.error(e.getMessage());
			}
			if (countTries > countTimes) {
				break;
			}
		}
	}
}
