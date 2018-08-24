package cz.xtf.radanalytics.driver.build;

import cz.xtf.radanalytics.driver.deployment.DriverBuilder;
import cz.xtf.radanalytics.util.ProjectFinder;
import cz.xtf.radanalytics.util.TestHelper;

import cz.xtf.git.GitProject;
import cz.xtf.openshift.OpenShiftUtil;
import cz.xtf.radanalytics.configuration.RadanalyticsConfiguration;
import io.fabric8.openshift.api.model.BuildConfig;
import io.fabric8.openshift.api.model.ImageStream;
import lombok.Getter;

public class DriverBuildDefinition {

	private static final OpenShiftUtil openshiftBuildsProject = TestHelper.openshiftBuildsProject();

	private String appName;
	private String appSubModule;

	@Getter
	private String buildName;
	private String imageName;
	private String appFile;

	private GitProject gitProject;

	@Getter
	private BuildConfig buildConfig;
	@Getter
	private ImageStream imageStream;

	private boolean buildCreated = false;

	public DriverBuildDefinition(String appName, String appSubModule, String buildName, String imageName, String appFile) {

		this.appName = appName;
		this.appSubModule = appSubModule;

		this.buildName = buildName;
		this.imageName = imageName;
		this.appFile = appFile;

		//we can create all builds immediately when starting the testsuite
		// or create specific build on demand
		if(RadanalyticsConfiguration.shouldBuildAll()){
			createBuilds();
		}
	}


	public void createBuilds() {
		if (!buildCreated) {
			gitProject = TestHelper.gitlab()
					.createProjectFromPath(appName, ProjectFinder.findApplicationDirectory(appName, appSubModule));

			DriverBuilder driverBuilder = new DriverBuilder(buildName, gitProject.getUrl(), imageName, appFile);
			this.imageStream = driverBuilder.GenerateImageStream();
			this.buildConfig = driverBuilder.GenerateBuildConfig();

			openshiftBuildsProject.createImageStream(imageStream);
			openshiftBuildsProject.createBuildConfig(buildConfig);

			buildCreated = true;
		}
	}
}
