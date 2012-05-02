package net.sf.gripes

import static org.junit.Assert.*
import net.sf.gripes.base.BaseGripesPluginTest

import org.gradle.api.*
import org.gradle.api.tasks.TaskExecutionException
import org.gradle.testfixtures.*
import org.junit.*
import org.junit.Rule
import org.junit.rules.ExpectedException

@Ignore
class GripesPluginTest extends BaseGripesPluginTest {	
	@Ignore
	@Test void gradleHasGripes() {
		assertTrue(project.plugins.hasPlugin(GripesPlugin))		
		assertTrue(project.plugins.findPlugin("gripes") instanceof GripesPlugin)
	}
	
	@Ignore
    @Test void gradleHasGripesTasks() {
		assertTrue(project.tasks.init instanceof Task)
		assertTrue(project.tasks.create.dependsOn.contains(project.tasks.compileGroovy))
    }
	
	@Ignore
	@Test void gripesInitTask() {
		File srcDir = new File(gripesConvention.base.canonicalPath+gripesConvention.src)
		new groovy.util.AntBuilder().delete(dir: gripesConvention.base.canonicalPath+gripesConvention.src)
		
		
		project.tasks.init.execute()
		assert(new File(gripesConvention.base.canonicalPath+gripesConvention.src).exists())
		
		srcDir.delete()
	}
	
	@Ignore
	@Test void gripesInitTaskError() {
		File srcDir = new File(gripesConvention.base.canonicalPath+gripesConvention.src)
		srcDir.mkdirs()
		
		try {
			project.tasks.init.execute()
		} catch (TaskExecutionException e) {
			assert(e.cause instanceof IllegalStateException)
		}
	}
	
	@Ignore
	@Test void gripesSetupTask() {
		File srcDir = new File(gripesConvention.base.canonicalPath+gripesConvention.src+"/META-INF")
		srcDir.delete()
		
		project.tasks.setup.execute()
		assert(srcDir.exists())
		
		srcDir.delete()
	}
	
	@Ignore
	@Test void gripesSetupTaskError() {
		File srcDir = new File(gripesConvention.base.canonicalPath+gripesConvention.src+"/META-INF")
		srcDir.mkdirs()
		
		try {
			project.tasks.setup.execute()
		} catch (TaskExecutionException e) {
			assert(e.cause instanceof IllegalStateException)
		}
	}
	
	@Ignore
	@Test void gripesNewPlugin() {
		File gripesCfg = creator.getGripesConfigFile()
		String backupTxt = gripesCfg.text
		String newAddon = "gripes-addons-example"
		project.with {
			setProperty("addon", newAddon)
			
			dependencies {
				addons "gripes-addons:gripes-addon-example:0.1.1"
			}
			
			tasks.install.execute()
		}
		
		assert gripesCfg.text.find("\"${newAddon}\"")
		
		gripesCfg.text = backupTxt
	}
//	@Test void gripesSrcPlugin() {
//		File gripesCfg = creator.getGripesConfigFile()
//		String backupTxt = gripesCfg.text
//		String newAddon = "gripes-addons-example"
//					
//		File rootDir = new File(System.getProperty("java.io.tmpdir"))
//		File projectDir = new File(rootDir, "project");
//		projectDir.mkdirs()
//		File buildFile = new File(projectDir, "build.gradle");
//		buildFile.deleteOnExit()
//		FileUtils.writeStringToFile(buildFile, "println 'test'");
//		
//		def proc = "/Users/cody/Development/gradle-1.0-m6/bin/gradle --help".execute([], projectDir)
//		proc.waitFor()
//
//		println "return code: ${ proc.exitValue()}"
//		println "stderr: ${proc.err.text}"
//		println "stdout: ${proc.in.text}"
//		
////		File settingsFile = new File(project.getProjectDir().canonicalPath+"/settings.gradle")
////		settingsFile.createNewFile()
////		settingsFile.text = "include '$newAddon'"
////		println settingsFile.text
//		
////		ProjectBuilder topProjectBuilder = ProjectBuilder.builder().withName("topProject")
////		Project topProject = projectBuilder.build()
////		Project addonProject = ProjectBuilder
////								.builder()
////								.withName("gripes-addon-example")
////								.withProjectDir(new File('../${newAddon}'))
////								.withParent(topProject)
////								.build()
////		topProject.with {
////			apply {
////				plugin 'groovy'
//////				plugin 'war'
//////				plugin 'gripes'
////			}
//////			setProperty("addon", "gripes-addon-example")
////			
////			configurations {
////				addons{}
////			}
////			
////			dependencies {
////				compile project(":gripes-addon-example")
////			}
////		}
//////		println topProject.configurations.compile.getResolvedConfiguration().getResolvedArtifacts()
//////		println "Reso: " + topProject.configurations.compile.getResolutionStrategy()
//////		topProject.tasks.install.execute()
////		println "subs: "+ topProject.subprojects
////		println "TEMP: "+ testDir
//		
////		assert gripesCfg.text.find("\"${newAddon}\"")
////		gripesCfg.text = backupTxt
//	}
}