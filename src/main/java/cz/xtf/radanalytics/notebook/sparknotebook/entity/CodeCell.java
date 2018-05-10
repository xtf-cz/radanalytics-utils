package cz.xtf.radanalytics.notebook.sparknotebook.entity;

public interface CodeCell {

	CodeCell runCell();

	String getOutput();

	boolean outputHasErrors();

}
