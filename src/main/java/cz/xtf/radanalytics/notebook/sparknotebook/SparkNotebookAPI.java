package cz.xtf.radanalytics.notebook.sparknotebook;

import cz.xtf.radanalytics.notebook.sparknotebook.entity.CodeCell;

public interface SparkNotebookAPI {

	void loadProject(String projectName);

	void loadProjectByURL(String projectName);

	CodeCell getNthCodeCell(int n);

	void webDriverCleanup();

	void assertCodeCellSpark(int cellIndex);

	void assertCodeCellRangeSpark(int start, int end);
}
