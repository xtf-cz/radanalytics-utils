package cz.xtf.radanalytics.oshinko.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

/**
 * Java object representing Oshinko JSON object
 */
@Getter
@Setter
public class SparkPod {

	@JsonProperty("IP")
	private String ip;
	@JsonProperty("Status")
	private String status;
	@JsonProperty("Type")
	private String type;
	
}
