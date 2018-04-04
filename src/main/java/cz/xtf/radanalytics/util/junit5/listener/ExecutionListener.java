package cz.xtf.radanalytics.util.junit5.listener;

import cz.xtf.time.TimeUtil;
import lombok.extern.slf4j.Slf4j;

import org.junit.platform.engine.TestExecutionResult;
import org.junit.platform.launcher.TestExecutionListener;
import org.junit.platform.launcher.TestIdentifier;
import org.junit.platform.launcher.TestPlan;

import java.util.HashMap;
import java.util.Map;

@Slf4j
public class ExecutionListener implements TestExecutionListener {
	Map<String, Long> executionTimes = new HashMap<>();

	@Override
	public void testPlanExecutionStarted(TestPlan testPlan) {
		// TODO project creation if none  exist?
	}

	@Override
	public void testPlanExecutionFinished(TestPlan testPlan) {
		//
		// TODO delete created project based on env
	}

	@Override
	public void executionSkipped(TestIdentifier testIdentifier, String reason) {
		String displayName = getTestDisplayName(testIdentifier);
		log.info("*** {} has been skipped ({}). ***", displayName, reason);
	}

	@Override
	public void executionStarted(TestIdentifier testIdentifier) {
		executionTimes.put(testIdentifier.getUniqueId(), System.currentTimeMillis());
		if (testIdentifier.isTest()) {
			String displayName = getTestDisplayName(testIdentifier);
			log.info("*** {} is starting ***", displayName);
		}
	}

	@Override
	public void executionFinished(TestIdentifier testIdentifier, TestExecutionResult testExecutionResult) {
		Long executionTime = System.currentTimeMillis() - executionTimes.remove(testIdentifier.getUniqueId());
		if (testIdentifier.isTest()) {
			String displayName = getTestDisplayName(testIdentifier);
			String status = resolveStatusString(testExecutionResult);
			log.info("*** {} {} after {}. ***", displayName, status, TimeUtil.millisToString(executionTime));
		}
	}

	private String resolveStatusString(TestExecutionResult testExecutionResult) {
		switch (testExecutionResult.getStatus()) {
			case SUCCESSFUL:
				return "succeeded";
			case FAILED:
				return "failed";
			case ABORTED:
				return "has been aborted";
			default:
				return "is unknown";
		}
	}

	/**
	 * Parses testIdentifier parent id in format: 	 * '[engine:test-engine]/[class:full.class.name.MyTest]/...' (May or may not continue. Eg. parameterized tests). 	 * 	 * @param testIdentifier testIdentifier (isTest condition should be met, otherwise behaviour is undefined) 	 * @return Customized test display name
	 */
	private String getTestDisplayName(TestIdentifier testIdentifier) {
		String className = testIdentifier.getParentId().get()
				.replaceAll(".*class:", "")        // cut of everything before class: string
				.replaceAll("].*", "")                        // cut of everything after full class name
				.replaceAll(".*\\.", "");                    // cut of everyting before dot (output: class name)
		String methodName = testIdentifier.getLegacyReportingName()
				.replaceAll("\\(.*", "");                    // cut of method params (usually just ())
		
		String baseName = String.format("%s::%s", className, methodName);
		String displayName = testIdentifier.getDisplayName();
		if (!displayName.isEmpty() && !displayName.equals(testIdentifier.getLegacyReportingName())) {
			baseName = baseName.concat(String.format("(%s)", displayName));
		}
		return baseName;
	}
}