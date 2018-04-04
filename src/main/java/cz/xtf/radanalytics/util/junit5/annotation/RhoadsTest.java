package cz.xtf.radanalytics.util.junit5.annotation;


import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.extension.ExtendWith;

import cz.xtf.radanalytics.util.junit5.extension.ProjectCleaner;
import cz.xtf.radanalytics.util.junit5.extension.TestLogger;

import java.lang.annotation.*;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@ExtendWith({ProjectCleaner.class, TestLogger.class})
@Tag("rhoads-test")
public @interface RhoadsTest {
}
