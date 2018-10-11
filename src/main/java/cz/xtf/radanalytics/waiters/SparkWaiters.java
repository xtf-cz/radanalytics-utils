package cz.xtf.radanalytics.waiters;

import java.io.IOException;
import java.util.Collection;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

import cz.xtf.openshift.logs.LogCheckerUtils;
import cz.xtf.wait.SimpleWaiter;
import cz.xtf.wait.Waiter;
import io.fabric8.kubernetes.api.model.Pod;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class SparkWaiters {

	private static final Pattern PATTERN_WORKERS_REGISTERED = Pattern.compile("Successfully registered with master spark://");

	/**
	 * Creates a waiter that waits until all Spark Workers are Register with Spark Master
	 * Defaults to 3 minutes timeout.
	 *
	 * @param workerPods worker pods to wait for
	 * @return Waiter instance
	 */
	public static Waiter areSparkWorkersRegisteredWithMaster(Collection<Pod> workerPods) {
		return new SimpleWaiter(() -> isPatternFound(workerPods, PATTERN_WORKERS_REGISTERED), TimeUnit.MINUTES, 3L);
	}

	/**
	 * Creates a waiter that waits until a pattern will be found in the logs on the pod
	 * Defaults to 3 minutes timeout.
	 *
	 * @param pod a pod that contains the logs with the pattern
	 * @param patternInLogs the pattern which should be in the logs on the pod
	 * @return Waiter instance
	 */
	public static Waiter waitForPatternFound(Pod pod, Pattern patternInLogs) {
		return new SimpleWaiter(() -> isPatternFound(pod, patternInLogs), TimeUnit.MINUTES, 5L);
	}

	/**
	 * Creates a method that is trying to find a particular pattern in logs on the pod
	 *
	 * @param pod a pod that contains logs with the pattern
	 * @param patternInLogs a pattern which should be in the logs on the pod
	 * @return a boolean value
	 */
	private static boolean isPatternFound(Pod pod, Pattern patternInLogs) {
		boolean found[];
		try {
			log.debug("Trying to find pattern \"{}\"", patternInLogs);
			found = LogCheckerUtils.findPatternsInLogs(pod, patternInLogs);
		} catch (IOException e) {
			throw new IllegalStateException("Wasn't able to check Pod's logs.", e);
		}

		if (found == null) {
			log.info("Pattern : '{}' - not found in log.", patternInLogs);
			return false;
		}

		return found[0];
	}

	/**
	 * Creates a method that is trying to find a particular pattern in logs on all pods
	 *
	 * @param pods pods where pattern should be found
	 * @return a boolean value
	 */
	private static boolean isPatternFound(Collection<Pod> pods, Pattern patternInLogs) {
		boolean found[];
		try {
			log.debug("Trying to find pattern \"{}\"", patternInLogs);
			found = LogCheckerUtils.findPatternsInLogs(pods, patternInLogs);
		} catch (IOException e) {
			throw new IllegalStateException("Wasn't able to check Pod's logs.", e);
		}
		return found[0];
	}
}