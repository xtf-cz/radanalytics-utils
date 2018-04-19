package cz.xtf.radanalytics.oshinko.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

/**
 * Java object representing Oshinko JSON object
 */
@Slf4j
@Getter
@Setter
public class SparkCluster {
	@JsonProperty("name")
	private String clusterName;
	@JsonProperty("masterCount")
	private int mastersCount;
	@JsonProperty("workerCount")
	private int workersCount;

	@JsonProperty("namespace")
	private String namespace;
	@JsonProperty("href")
	private String href;
	@JsonProperty("image")
	private String image;
	@JsonProperty("masterUrl")
	private String masterUrl;
	@JsonProperty("masterWebUrl")
	private String masterWebUrl;
	@JsonProperty("status")
	private String status;

	@JsonProperty("Config")
	private SparkConfig sparkConfig;
	@JsonProperty("Pods")
	private List<SparkPod> sparkPods;
}


	