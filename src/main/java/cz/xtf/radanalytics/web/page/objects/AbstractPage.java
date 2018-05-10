package cz.xtf.radanalytics.web.page.objects;

import cz.xtf.radanalytics.waiters.WebWaiters;
import cz.xtf.radanalytics.web.extended.elements.ExtendedFieldDecorator;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.PageFactory;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
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
				pageLoaded(2000L, navigateToPageUrl, 3);
				webDriver.get(navigateToPageUrl);
			}
		}
	}

	private static void pageLoaded(Long interval, String url, int countTimes) {
		URL link = null;
		try {
			link = new URL(url);
		} catch (MalformedURLException e) {
			log.error(String.format("The following url provided is malformed: %s", url));
			log.error(e.getMessage());
		}
		URL finalLink = link;
		BooleanSupplier successConditionForConnection = () -> {
			try {
				HttpURLConnection connection = (HttpURLConnection) finalLink.openConnection();
				connection.setRequestMethod("GET");
				connection.connect();
				return connection.getResponseCode() == 200;
			} catch (IOException e) {
				log.error(e.getMessage());
				return false;
			}
		};
		int countTries = 0;
		while (!successConditionForConnection.getAsBoolean()) {
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
