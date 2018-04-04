package cz.xtf.radanalytics.oshinko.cli.service;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import cz.xtf.radanalytics.oshinko.entity.SparkCluster;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

@Slf4j
public class SparkClusterService {

	private static List<SparkCluster> sparkClustersFromText(String text, String type) {
		log.debug("Start search Spark Cluster in {} file : {}", type, text);
		ObjectMapper mapper = new ObjectMapper();
		mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);

		SparkCluster[] clusters = null;

		try {
			clusters = mapper.readValue(text, SparkCluster[].class);
		} catch (IOException e) {
			log.error("Can't parse {} file to array of SparkCluster.class", type, e);
			throw new IllegalStateException("Can't parse file to array of SparkCluster.class", e);
		}

		if (clusters == null) {
			log.debug("Spark Cluster was not found.");
			return null;
		}
		log.debug("Spark Cluster was found : {}.", clusters.toString());
		return Arrays.asList(clusters);
	}

	public static List<SparkCluster> sparkClustersFromJson(String json) {
		return sparkClustersFromText(json, "JSON");
	}

	public static List<SparkCluster> sparkClustersFromYaml(String yaml) {
		return sparkClustersFromText(yaml, "YAML");
	}

}
