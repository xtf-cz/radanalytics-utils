package cz.xtf.radanalytics.notebook.jupyter.page.objects;

import cz.xtf.radanalytics.web.extended.elements.elements.Button;
import cz.xtf.radanalytics.web.extended.elements.elements.TextField;
import cz.xtf.radanalytics.web.page.objects.AbstractPage;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.FindBy;

public class LoginPage extends AbstractPage {
	@FindBy(xpath = "//input[@id='password_input']")
	private TextField passwordField;

	@FindBy(xpath = "//button[@id='login_submit']")
	private Button loginButton;

	public LoginPage(WebDriver webDriver, String hostname, boolean navigateToPage) {
		super(webDriver, hostname, navigateToPage, "http://" + hostname + "/login");
	}

	public LoginPage fillPassword(String password) {
		passwordField.sendKeys(password);
		return this;
	}

	public JupiterTreePage clickOnLoginButton() {
		loginButton.click();
		return new JupiterTreePage(webDriver, hostname, false);
	}


}
