package cz.xtf.radanalytics.openshift.openshift.web;

import cz.xtf.radanalytics.openshift.openshift.web.page.objects.AuthorizeAccessPage;
import cz.xtf.radanalytics.openshift.openshift.web.page.objects.LoginPage;
import cz.xtf.radanalytics.oshinko.web.OshinkoPoddedWebUI;
import cz.xtf.radanalytics.web.webdriver.AbstractWebDriver;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class OpenshiftWebUI extends AbstractWebDriver implements OpenshiftWebApi {
	private final String hostname;

	private OpenshiftWebUI(String hostname) {
		super();
		this.hostname = hostname;
		log.info("Init Openshift Web UI");
	}

	public static OpenshiftWebUI getInstance(String hostname) {
		return new OpenshiftWebUI(hostname);
	}

	@Override
	public boolean openShiftLogin(String userName, String pass) {
		new LoginPage(webDriver, hostname, true)
				.fillUserNameField(userName)
				.fillPasswordField(pass)
				.clickOnLoginButton();
		return true;
	}

	@Override
	public OshinkoPoddedWebUI allowSelectedPermissions() {
		return new AuthorizeAccessPage(webDriver, hostname, false)
				.allowPermissions();
	}


}
