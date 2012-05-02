package net.sf.gripes.base

import net.sf.gripes.GripesCreate;
import net.sf.gripes.GripesPluginConvention;

import org.gradle.api.Project;
import org.gradle.testfixtures.ProjectBuilder;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.rules.ExpectedException;

class BaseGripesPluginTest {
	static Project project
	static ProjectBuilder projectBuilder
	static GripesCreate creator
	static GripesPluginConvention gripesConvention	
	
	@Rule public ExpectedException exception = ExpectedException.none();
	
	@BeforeClass static void setupGripesTests() {
		projectBuilder = ProjectBuilder
							.builder()
							.withName("testProject")
							.withProjectDir(new File('test-resources'))
							
		project = projectBuilder.build()
		project.with {
			apply {
				plugin 'war'
				plugin 'groovy'
				plugin 'gripes'
			}
			
			repositories {
				mavenCentral()
				mavenRepo url: "http://www.gripes-project.org/libs"
			}
			
			configurations {
				addons{}
			}
		}
		  
		gripesConvention = (GripesPluginConvention) project.getConvention().getPlugins().get("gripes")
		gripesConvention.with {
			base = new File("test/mock-gripes")
			src = "/src"
			resources = "/resources"
			packageBase = "com.acme"
		}
		
		creator = new GripesCreate(project: project)
	}
}
