package cz.xtf.radanalytics.notebook.zeppelin.entity;

public interface Paragraph {

	Paragraph runParagraph();

	String getOutput();

	boolean outputHasErrors();

}
