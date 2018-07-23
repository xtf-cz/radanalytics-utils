package cz.xtf.radanalytics.notebook.zeppelin;

import cz.xtf.radanalytics.notebook.zeppelin.page.object.ZeppelinNotebookPage;

public class ZeppelinWebImpl implements ZeppelinAPI{

	private ZeppelinNotebookPage notebookPage;

	public ZeppelinWebImpl(ZeppelinNotebookPage notebookPage) {
		this.notebookPage = notebookPage;
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
