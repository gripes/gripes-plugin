package net.sf.gripes

import static org.junit.Assert.*

import java.io.File

import org.gradle.api.*
import org.gradle.testfixtures.*
import org.junit.*


@Ignore
class GripesCreateTest {
	static Project project
	static GripesCreate creator
	static GripesPluginConvention gripesConvention
	
	@BeforeClass static void setupGripesTests() {
		project = ProjectBuilder.builder().withProjectDir(new File('test-resources')).build()
		project.plugins.apply 'war'
		project.plugins.apply 'groovy'
	  	project.plugins.apply 'gripes'
		  
		gripesConvention = (GripesPluginConvention) project.getConvention().getPlugins().get("gripes")
		gripesConvention.with {
			base = new File("test/mock-gripes")
			src = "/src"
			resources = "/resources"
			packageBase = "com.acme"
		}
		  
	  	creator = new GripesCreate([project: project])
	}

	@Ignore
	@Test void gradleHasGripes() {
		assert project.plugins.findPlugin("gripes") != null
	}
	
	@Ignore
	@Test void initGripesProject() {
		creator.init()
		assert(new File(gripesConvention.base.canonicalPath+"/src").exists())
	}
	
	@Ignore
	@Test void setupGripesProject() {
		creator.setup()
		assert(new File(gripesConvention.base.canonicalPath+"/web/style").exists())
		assert(new File(gripesConvention.base.canonicalPath+"/src/META-INF").exists())
	}
	
	@Ignore
	@Test void createProperFields() {
		assert creator.views("Page","com.acme") == ['list']		
	}
	
	@Ignore
	@Test void deleteGripesProject() {
		creator.delete()
		["src","web","resources","conf"].each {
			assert(!(new File(gripesConvention.base.canonicalPath+"/"+it).exists()))
		}
	}
}