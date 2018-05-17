package cz.xtf.radanalytics.util.junit5.extension;

import cz.xtf.radanalytics.web.webdriver.LocalWebDriverManager;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.extension.AfterTestExecutionCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriverException;

import java.io.File;
import java.io.IOException;

@Slf4j
public class ScreenShotter implements AfterTestExecutionCallback {

	@Override
	public void afterTestExecution(ExtensionContext extensionContext) throws Exception {

		if (extensionContext.getExecutionException().isPresent()) {

			String className = extensionContext.getTestClass().get().getSimpleName();
			String testName = extensionContext.getTestMethod().get().getName();

			takeScreenshot(className + "." + testName);
		}
	}

	public static void takeScreenshot(String testId) {
		if (LocalWebDriverManager.get().getWebDriver() != null) {
			try {
				File scrFile = ((TakesScreenshot) LocalWebDriverManager.get().getWebDriver()).getScreenshotAs(OutputType.FILE);
				try {
					FileUtils.copyFile(scrFile, new File(new File("log"), testId + ".png"));
					log.info("*** Screenshot taken: {}.png ", testId );
				} catch (IOException e1) {
					log.error(e1.getMessage());
				}
			} catch (WebDriverException x) {
				// ignore, we don't have a window to shoot, or any other problem with screenshotting
			}
		}
	}
}
