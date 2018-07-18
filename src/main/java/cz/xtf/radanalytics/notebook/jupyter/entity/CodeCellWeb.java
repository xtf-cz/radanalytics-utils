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
		log.debug("Getting input code cell prompt");
		return projectPage.getInputCodeCellPrompt(CELL);
	}

	@Override
	public CodeCell runCell() {
		log.debug("Executing code cell");
		projectPage
				.clickOnCell(CELL)
				.clickOnRunButton();
		log.debug("Input prompt updates to next execution count after code finishes executing");
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
		log.debug("Getting output execution code cell without timeout");
		return new ProjectPage(webDriver, false).getOutputArea(CELL);
	}

	@Override
	public String getOutput(Long timeout) {
		log.debug("Getting output execution code cell with timeout {}", timeout);
		if (executionCount < 1) {
			throw new RuntimeException("Code cell must be executed at least once to retrieve output.");
		}
		return new ProjectPage(webDriver, false).getOutputArea(CELL, timeout);
	}

	@Override
	public boolean outputHasErrors() {
		log.debug("Checking if output has errors");
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
		log.debug("Getting current code line");
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
		log.debug("Finding the required cell and replacing code in according cell");
		try {
			projectPage
					.clickOnCell(CELL)
					.clickOnEditTab()
					.chooseItemInDropDownMenu("Find and Replace")
					.fillFindFieldInFindAndReplaceModal(find)
					.fillReplaceFieldInFindAndReplaceModal(replace)
					.clickOnReplaceAllButton();

		} catch (Exception e) {
			return false;
		}

		return true;
	}

	@Override
	public boolean addBelowCellAndInsertCode(String code) {
		log.debug("Adding new cell bellow and inserting code into it");
		try {
			projectPage
					.addCellBellow()
					.insertCodeIntoSelectedCell(code);
		} catch (Exception e) {
			return false;
		}

		return true;
	}

	@Override
	public boolean mergeCellAbove() {
		log.debug("Marge cell above");
		try {
			projectPage
					.clickOnCell(CELL)
					.clickOnEditTab()
					.chooseItemInDropDownMenu("Merge Cell Above");
		} catch (Exception e) {
			return false;
		}

		return true;
	}

}
