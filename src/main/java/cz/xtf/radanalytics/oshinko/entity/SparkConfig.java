package cz.xtf.radanalytics.oshinko.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

/**
 * Java object representing Oshinko JSON object
 */
@Getter
@Setter
public class SparkConfig {

	@JsonProperty("MasterCount")
	private int masterCount;
	@JsonProperty("WorkerCount")
	private int workerCount;
	@JsonProperty("Name")
	private String name;
	@JsonProperty("SparkMasterConfig")
	private String sparkMasterConfig;
	@JsonProperty("SparkWorkerConfig")
	private String sparkWorkerConfig;
	@JsonProperty("SparkImage")
	private String sparkImage;

}
