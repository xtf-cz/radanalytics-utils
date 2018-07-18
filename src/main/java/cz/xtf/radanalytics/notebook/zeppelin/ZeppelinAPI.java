package cz.xtf.radanalytics.notebook.zeppelin;

import cz.xtf.radanalytics.notebook.zeppelin.page.object.ZeppelinPage;

public interface ZeppelinAPI {
	ZeppelinPage login (String username, String password);

}
