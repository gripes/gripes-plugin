package net.sf.gripes

import static org.junit.Assert.*;

import java.text.RBCollationTables.BuildAPI;

import org.junit.*
import org.gradle.api.*
import org.gradle.testfixtures.*

class GripesCreateTest {
	static Project project
	static GripesCreate creator
	
	@BeforeClass static void setupGripesTests() {
		project = ProjectBuilder.builder().withProjectDir(new File('test-resources')).build()
		project.plugins.apply 'war'
		project.plugins.apply 'groovy'
	  	project.plugins.apply 'gripes'
		  
	  	creator = new GripesCreate([project: project])
	}

	@Test void gradleHasGripes() {
		assert project.plugins.findPlugin("gripes") != null
	}
	
	@Test void createProperFields() {
		assert creator.views("Page","com.acme") == ['list']		
	}
}