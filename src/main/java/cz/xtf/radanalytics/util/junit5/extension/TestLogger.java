package cz.xtf.radanalytics.util.junit5.extension;

import org.junit.jupiter.api.extension.AfterTestExecutionCallback;
import org.junit.jupiter.api.extension.BeforeTestExecutionCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ExtensionContext.Namespace;
import org.junit.jupiter.api.extension.ExtensionContext.Store;

import java.lang.reflect.Method;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class TestLogger implements BeforeTestExecutionCallback, AfterTestExecutionCallback {

	@Override
	public void beforeTestExecution(ExtensionContext extensionContext) throws Exception {
		getStore(extensionContext).put(extensionContext.getRequiredTestMethod(), System.currentTimeMillis());

		String className = extensionContext.getTestClass().get().getSimpleName();
		String testName = extensionContext.getTestMethod().get().getName();
		String displayName = extensionContext.getDisplayName();

		log.info("*** Test {}::{}({}) is starting ***", className, testName, displayName.startsWith(testName) ? "" : displayName);
	}

	@Override
	public void afterTestExecution(ExtensionContext extensionContext) throws Exception {
		String className = extensionContext.getTestClass().get().getSimpleName();
		String testName = extensionContext.getTestMethod().get().getName();

		Method testMethod = extensionContext.getRequiredTestMethod();

		long start = getStore(extensionContext).remove(testMethod, long.class);
		long seconds = (System.currentTimeMillis() - start) / 1000;

		log.info("*** Test {}::{} has ended in {} ***", className, testName, seconds);
	}

	private Store getStore(ExtensionContext context) {
		return context.getStore(Namespace.create(getClass(), context));
	}
}