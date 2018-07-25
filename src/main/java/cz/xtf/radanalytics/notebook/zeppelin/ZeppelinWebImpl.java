package cz.xtf.radanalytics.notebook.zeppelin;

import cz.xtf.radanalytics.notebook.zeppelin.page.object.ZeppelinJobPage;
import cz.xtf.radanalytics.notebook.zeppelin.page.object.ZeppelinNotebookPage;
import cz.xtf.radanalytics.notebook.zeppelin.page.object.ZeppelinPage;


public class ZeppelinWebImpl implements ZeppelinAPI{

	private ZeppelinNotebookPage notebookPage;
	private ZeppelinJobPage jobPage;
	private ZeppelinPage mainPage;


	public ZeppelinWebImpl(ZeppelinNotebookPage notebookPage) {
		this.notebookPage = notebookPage;
	}

	public ZeppelinWebImpl(ZeppelinJobPage jobPage) {
		this.jobPage = jobPage;
	}

	public ZeppelinWebImpl(ZeppelinPage mainPage) {
		this.mainPage = mainPage;
	}

	public ZeppelinWebImpl(ZeppelinNotebookPage notebookPage, ZeppelinJobPage jobPage, ZeppelinPage mainPage) {
		this.notebookPage = notebookPage;
		this.jobPage = jobPage;
		this.mainPage = mainPage;
	}

	@Override
	public void runAllParagraphs() {
		notebookPage.runAllParagraphs().confirmRunAllParagraphs();
	}

	@Override
	public boolean outputHasErrors() {
		return notebookPage.areErrorMessageEnabled();
	}
}
