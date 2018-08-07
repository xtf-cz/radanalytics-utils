package cz.xtf.radanalytics.notebook.jupyter.page.objects;

import cz.xtf.radanalytics.util.junit5.annotation.WebUITests;
import cz.xtf.radanalytics.waiters.WebWaiters;
import cz.xtf.radanalytics.web.extended.elements.elements.Button;
import cz.xtf.radanalytics.web.page.objects.AbstractPage;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.FindBy;

@WebUITests
public class LoginPage extends AbstractPage {
	@FindBy(xpath = "//button[@id='login_submit']")
	private Button loginButton;

	private final String passwordField = "//input[@id='password_input']";

	public LoginPage(WebDriver webDriver, String hostname, boolean navigateToPage) {
		super(webDriver, hostname, navigateToPage, "http://" + hostname + "/login");
	}

	public LoginPage fillPassword(String password) {
		WebWaiters.waitUntilElementIsPresent(passwordField, webDriver);
		webDriver.findElement(By.xpath(passwordField)).sendKeys(password);
		return this;
	}

	public JupiterTreePage clickOnLoginButton() {
		loginButton.click();
		return new JupiterTreePage(webDriver, hostname, false);
	}


}
