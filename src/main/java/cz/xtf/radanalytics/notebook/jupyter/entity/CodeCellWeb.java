package cz.xtf.radanalytics.notebook.jupyter.entity;

import cz.xtf.radanalytics.notebook.jupyter.page.objects.ProjectPage;
import cz.xtf.radanalytics.waiters.WebWaiters;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import java.util.concurrent.TimeoutException;
import java.util.function.BooleanSupplier;

@Slf4j
public class CodeCellWeb implements CodeCell {
	private static ProjectPage projectPage;
	private final WebElement CELL;
	private final WebDriver webDriver;
	private int executionCount;

	public CodeCellWeb(WebElement cell, WebDriver webDriver) {
		this.CELL = cell;
		this.webDriver = webDriver;
		projectPage = new ProjectPage(webDriver, false);
	}

	@Override
	public char getInputPrompt() {
		return projectPage.getInputCodeCellPrompt(CELL);
	}

	@Override
	public CodeCell runCell() {
		projectPage
				.clickOnCell(CELL)
				.clickOnRunButton();

		// Input prompt updates to next execution count after code finishes executing
		BooleanSupplier successCondition = () -> {
			char inputPrompt = getInputPrompt();
			if (Character.isDigit(inputPrompt)) {
				int newExecutionCount = inputPrompt - '0';
				if (newExecutionCount > this.executionCount) {
					this.executionCount = newExecutionCount;
					return true;
				}
				return false;
			} else if (inputPrompt == ' ' || inputPrompt == '*') {
				return false;
			} else {
				throw new RuntimeException("Input Prompt in an unknown state after Cell execution.");
			}
		};

		try {
			WebWaiters.waitFor(successCondition);
		} catch (InterruptedException | TimeoutException e) {
			log.error(e.getMessage());
		}

		return this;
	}

	@Override
	public String getOutput() {
		return new ProjectPage(webDriver, false).getOutputArea(CELL);
	}

	@Override
	public String getOutput(Long timeout) {
		if (executionCount < 1) {
			throw new RuntimeException("Code cell must be executed at least once to retrieve output.");
		}
		return new ProjectPage(webDriver, false).getOutputArea(CELL, timeout);
	}

	@Override
	public boolean outputHasErrors() {
		if (executionCount < 1) {
			throw new RuntimeException("Code cell must be executed at least once to retrieve output.");
		}

		String output = getOutput();
		return output.contains("Traceback (most recent call last)");
	}

	/**
	 * Return WebElement representation of code line at line number starting from 1.
	 */
	@Override
	public WebElement getCodeLine(int lineNumber) {
		return new ProjectPage(webDriver, false).getCodeLineContent(CELL, lineNumber);
	}

	@Override
	public boolean setCodeLine(WebElement element) {
		// Todo: Implement
		return false;
	}

	/**
	 * Find and replace a line of code in this code cell. Return true upon success.
	 */
	@Override
	public boolean findAndReplaceInCell(String find, String replace) {
		// Set search to this cell's scope
		try {
			projectPage
					.clickOnCell(CELL)
					.clickOnEditTab()
					.chooseItemInDropDownMenu("Find and Replace")
					.fillFindFieldInFindAndReplaceModal(find)
					.fillReplaceFielsInFindAndReplaceModal(replace)
					.clickOnReplaceAllButton();

		} catch (Exception e) {
			return false;
		}

		return true;
	}

}
