package cz.xtf.radanalytics.notebook.sparknotebook.page.objects;

import cz.xtf.radanalytics.web.page.objects.AbstractPage;
import org.openqa.selenium.WebDriver;

public class MainPage extends AbstractPage {
	public MainPage(WebDriver webDriver, String hostname, boolean navigateToPage, String navigateToPageUrl) {
		super(webDriver, hostname, navigateToPage, navigateToPageUrl);
	}
}
