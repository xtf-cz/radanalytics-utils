package cz.xtf.radanalytics.notebook.zeppelin;

public interface ZeppelinAPI {

	void runAllParagraphs();

	boolean outputHasErrors();

}
