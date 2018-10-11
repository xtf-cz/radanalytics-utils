package cz.xtf.radanalytics.util.junit5.annotation;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.extension.ExtendWith;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import cz.xtf.radanalytics.util.junit5.extension.ProjectCleaner;
import cz.xtf.radanalytics.util.junit5.extension.TestLogger;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@ExtendWith({ProjectCleaner.class, TestLogger.class})
@Tag("rhoads-test")
public @interface RhoadsTest {
}
