package cz.xtf.radanalytics.util.junit5.extension;

import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.ExtensionContext;

import cz.xtf.openshift.OpenShiftUtils;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ProjectCleaner implements BeforeAllCallback {

	@Override
	public void beforeAll(ExtensionContext extensionContext) throws Exception {
		log.info("Cleaning up project");
		OpenShiftUtils.master().clean();
	}
}