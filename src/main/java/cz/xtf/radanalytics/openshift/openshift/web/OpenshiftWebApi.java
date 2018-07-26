package cz.xtf.radanalytics.openshift.openshift.web;

import cz.xtf.radanalytics.oshinko.web.OshinkoPoddedWebUI;

public interface OpenshiftWebApi {
	boolean openShiftLogin(String userName, String pass);

	OshinkoPoddedWebUI allowSelectedPermissions();
}
