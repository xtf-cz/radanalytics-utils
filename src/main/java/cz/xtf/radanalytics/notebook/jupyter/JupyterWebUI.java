package cz.xtf.radanalytics.notebook.jupyter;

import org.assertj.core.api.Assertions;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import java.util.List;

import cz.xtf.radanalytics.notebook.jupyter.entity.CodeCell;
import cz.xtf.radanalytics.notebook.jupyter.entity.CodeCellWeb;
import cz.xtf.radanalytics.notebook.jupyter.page.objects.JupiterTreePage;
import cz.xtf.radanalytics.notebook.jupyter.page.objects.LoginPage;
import cz.xtf.radanalytics.notebook.jupyter.page.objects.ProjectPage;
import cz.xtf.radanalytics.web.WebHelpers;

public class JupyterWebUI implements JupyterAPI {
	private final WebDriver webDriver;
	private final String HOSTNAME;

	public JupyterWebUI(WebDriver webDriver, String hostname) {
		this.webDriver = webDriver;
		this.HOSTNAME = hostname;
	}

	@Override
	public void login(String password) {
		LoginPage loginPage = new LoginPage(webDriver, HOSTNAME, true);
		loginPage
				.fillPassword(password)
				.clickOnLoginButton();
	}

	@Override
	public void loadProject(String projectName) {
		new JupiterTreePage(webDriver, HOSTNAME, true)
				.clickOnFilesTab()
				.chooseProjectInTree(projectName);
		WebHelpers.switchWindowTab(webDriver, 1);
	}

	@Override
	public void loadProjectByURL(String projectName) {
		new ProjectPage(webDriver, HOSTNAME, true, projectName);
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
	public void assertCodeCell(int cellIndex) {
		assertCodeCellRange(cellIndex, cellIndex);
	}

	@Override
	public void assertCodeCellRange(int start, int end) {
		boolean outputHasErrors;
		for (int n = start; n <= end; n++) {
			try {
				outputHasErrors = this.getNthCodeCell(n).runCell().outputHasErrors();
				Assertions.assertThat(outputHasErrors).as("Check output status of cell %s", n).isFalse();
			} catch (AssertionError e) {
				Assertions.assertThat(e).hasMessage(String.format("Expected:<false> but was <%s>. With outputmessage: %s", false, this.getNthCodeCell(n).runCell().getOutput()));
			}
		}
	}

	@Override
	public void closeProject() {
		new ProjectPage(webDriver, false).closeAndHalt();
	}

	private List<WebElement> getAllCodeCells() {
		return new ProjectPage(webDriver, false).getAllCodeCells();
	}
}
