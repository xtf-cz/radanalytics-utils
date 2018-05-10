package cz.xtf.radanalytics.notebook.sparknotebook;

import cz.xtf.radanalytics.notebook.sparknotebook.entity.CodeCell;
import cz.xtf.radanalytics.notebook.sparknotebook.entity.CodeCellWeb;
import cz.xtf.radanalytics.notebook.sparknotebook.page.objects.ApplicationPage;
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

	private List<WebElement> getAllCodeCells() {
		return new ApplicationPage(webDriver, false).getAllCodeCells();
	}

}
