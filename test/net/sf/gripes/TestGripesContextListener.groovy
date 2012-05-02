package net.sf.gripes

import net.sf.gripes.base.BaseGripesPluginTest

import org.gradle.api.logging.Logger
import org.gradle.api.logging.Logging
import org.junit.Test

class TestGripesContextListener extends BaseGripesPluginTest {
	static Logger logger = Logging.getLogger(TestGripesContextListener.class)
		
	@Test void testFindGripesProperties() {
		Enumeration<URL> allResources = getClass().getClassLoader().getResources("META-INF/gripes/gripes.properties");
		
		allResources.each {
			logger.debug "URL: {}", it
		}
	}
}
