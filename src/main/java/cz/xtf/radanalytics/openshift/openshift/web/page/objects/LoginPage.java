package cz.xtf.radanalytics.openshift.openshift.web.page.objects;

import cz.xtf.radanalytics.waiters.WebWaiters;
import cz.xtf.radanalytics.web.extended.elements.elements.Button;
import cz.xtf.radanalytics.web.extended.elements.elements.TextField;
import cz.xtf.radanalytics.web.page.objects.AbstractPage;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.FindBy;

public class LoginPage extends AbstractPage {
	@FindBy(xpath = "//input[@id='inputUsername']")
	private TextField userNameField;

	@FindBy(xpath = "//input[@id='inputPassword']")
	private TextField passwordField;

	@FindBy(xpath = "//button[@type=\"submit\"]")
	private Button loginButton;

	public LoginPage(WebDriver webDriver, String hostname, boolean navigateToPage) {
		super(webDriver, hostname, navigateToPage, hostname);
	}

	public LoginPage fillUserNameField(String userName) {
		WebWaiters.waitUntilElementIsVisible(userNameField.getElement(), webDriver);
		userNameField.sendKeys(userName);
		return this;
	}

	public LoginPage fillPasswordField(String password) {
		passwordField.sendKeys(password);
		return this;
	}

	public LoginPage clickOnLoginButton() {
		loginButton.click();
		return this;
	}
}
