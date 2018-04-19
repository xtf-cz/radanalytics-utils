package cz.xtf.radanalytics.db;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class OpenshiftDB {

	private String username;
	private String password;
	private String adminUsername;
	private String adminPassword;
	private String connectionUrl;
	private String dbName;
	
}
