package cz.xtf.radanalytics.web.page.objects;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.PageFactory;

import java.util.Objects;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public abstract class AbstractPage {

	public final String hostname;
	public final WebDriver webDriver;

	public AbstractPage(WebDriver webDriver, String hostname, boolean navigateToPage, String navigateToPageUrl) {
		this.webDriver = webDriver;
		this.hostname = hostname;

		PageFactory.initElements(webDriver, this);

		if (navigateToPage) {

			if (!Objects.equals(webDriver.getCurrentUrl(), navigateToPageUrl)) {
				webDriver.get(navigateToPageUrl);
			}
		}
	}
}
