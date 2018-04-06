package cz.xtf.radanalytics.oshinko.web.page.objects;

import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.WebDriver;

@Slf4j
public abstract class _Page {

	public final String hostname;
	public final WebDriver webDriver;

	_Page(WebDriver webDriver, String hostname) {
		this.webDriver = webDriver;
		this.hostname = hostname;
	}
}
