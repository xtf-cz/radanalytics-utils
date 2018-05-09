package cz.xtf.radanalytics.notebook.sparknotebook;


import cz.xtf.radanalytics.notebook.sparknotebook.entity.CodeCell;

public interface SparkNotebookAPI {

	void loadProjectByURL(String projectName);

	CodeCell getNthCodeCell(int n);

	void webDriverCleanup();
}
