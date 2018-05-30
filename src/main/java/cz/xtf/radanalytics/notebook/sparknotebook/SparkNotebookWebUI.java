package cz.xtf.radanalytics.notebook.sparknotebook;

import cz.xtf.radanalytics.notebook.sparknotebook.entity.CodeCell;
import cz.xtf.radanalytics.notebook.sparknotebook.entity.CodeCellWeb;
import cz.xtf.radanalytics.notebook.sparknotebook.page.objects.ApplicationPage;
import org.assertj.core.api.Assertions;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import java.util.List;

public class SparkNotebookWebUI implements SparkNotebookAPI {
	private final WebDriver webDriver;
	private final String HOSTNAME;

	public SparkNotebookWebUI(WebDriver webDriver, String hostname) {
		this.webDriver = webDriver;
		this.HOSTNAME = hostname;
	}

	@Override
	public void loadProjectByURL(String projectName) {
		new ApplicationPage(webDriver, HOSTNAME, true, projectName);
	}

	@Override
	public CodeCell getNthCodeCell(int n) {
		if (n <= 0) {
			return null;
		}
		List<WebElement> codeCells = getAllCodeCells();

		return codeCells.isEmpty() ? null : new CodeCellWeb(codeCells.get(n - 1), webDriver);
	}

	@Override
	public void webDriverCleanup() {
		webDriver.quit();
	}

	@Override
	public void assertCodeCellSpark(int cellIndex) {
		assertCodeCellRangeSpark(cellIndex, cellIndex);
	}

	@Override
	public void assertCodeCellRangeSpark(int start, int end) {
		boolean outputHasErrors;
		CodeCell cell;
		for(int n = start; n <= end; n++){
			cell = this.getNthCodeCell(n);
			try {
				outputHasErrors = cell.runCell().outputHasErrors();
				Assertions.assertThat(outputHasErrors).as("Check output status of cell %s", n).isFalse();
			} catch (AssertionError e) {
				Assertions.assertThat(e).hasMessage(String.format("Expected:<false> but was <%s>. With outputmessage: %s",
						true, cell.getOutput()));
			}
		}
	}

	private List<WebElement> getAllCodeCells() {
		return new ApplicationPage(webDriver, false).getAllCodeCells();
	}

}
