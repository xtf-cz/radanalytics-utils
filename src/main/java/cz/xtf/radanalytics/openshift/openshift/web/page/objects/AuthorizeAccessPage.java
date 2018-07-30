package cz.xtf.radanalytics.openshift.openshift.web.page.objects;

import cz.xtf.radanalytics.oshinko.web.OshinkoPoddedWebUI;
import cz.xtf.radanalytics.waiters.WebWaiters;
import cz.xtf.radanalytics.web.extended.elements.elements.Button;
import cz.xtf.radanalytics.web.page.objects.AbstractPage;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.FindBy;

import java.net.MalformedURLException;
import java.net.URL;

@Slf4j
public class AuthorizeAccessPage extends AbstractPage {

	@FindBy(xpath = "//input[@name=\"approve\"]")
	private Button approveButton;

	public AuthorizeAccessPage(WebDriver webDriver, String hostname, boolean navigateToPage) {
		super(webDriver, hostname, navigateToPage, "");
	}

	public OshinkoPoddedWebUI allowPermissions() {
		try {
			String urlPath = new URL(webDriver.getCurrentUrl()).getPath();
			if (urlPath.contains("/oauth/authorize/")) {
				WebWaiters.waitUntilElementIsVisible(approveButton.getElement(), webDriver);
				approveButton.click();
			}
		} catch (MalformedURLException e) {
			log.debug(e.getMessage());
		}
		return OshinkoPoddedWebUI.getInstance(hostname);
	}
}
