package cz.xtf.radanalytics.notebook.sparknotebook.entity;

import cz.xtf.radanalytics.notebook.sparknotebook.page.objects.ApplicationPage;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import java.util.regex.Pattern;

public class CodeCellWeb implements CodeCell {
	private static ApplicationPage applicationPage;
	private final WebElement CELL;
	private final WebDriver webDriver;

	public CodeCellWeb(WebElement cell, WebDriver webDriver) {
		this.CELL = cell;
		this.webDriver = webDriver;
		applicationPage = new ApplicationPage(webDriver, false);
	}

	@Override
	public CodeCell runCell() {
		applicationPage
				.scrollToCell(CELL)
				.clickOnDropDownMenuToggle(CELL)
				.chooseClearCurrentOutput(CELL)
				.clickOnRunCodeButton(CELL)
				.isExecutionComlete(CELL);
		return this;
	}

	@Override
	public String getOutput() {
		return new ApplicationPage(webDriver, false).getOutputFromCell(CELL);
	}

	@Override
	public boolean outputHasErrors() {
		// If there are errors we expect an execution time to be displayed of the format
		// e.g. Took: 1.014s, at 2018-01-15 18:52

		String output = getOutput();
		return !Pattern.compile("Took:.*at").matcher(output).find();
	}

}
