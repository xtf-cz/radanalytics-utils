package cz.xtf.radanalytics.notebook.jupyter;


import cz.xtf.radanalytics.notebook.jupyter.entity.CodeCell;

public interface JupyterAPI {

	void login(String password);

	void loadProject(String projectName);

	void loadProjectByURL(String projectName);

	CodeCell getNthCodeCell(int n);

	void webDriverCleanup();

	void assertCodeCell(int cellIndex);

	void assertCodeCellRange(int start, int end);

	void closeProject();
}
