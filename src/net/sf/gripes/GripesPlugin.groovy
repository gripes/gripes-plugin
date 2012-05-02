package net.sf.gripes

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.plugins.JavaPluginConvention
import org.slf4j.Logger
import org.slf4j.LoggerFactory

/**
 * This is the heart of creating a Gripes application.
 * 
 * Creates the following new tasks: 
 * 	init
 * 	setup
 * 	create
 * 	run
 * 	stop
 * 	delete
 * 
 * Re-implements tasks:
 * 	war
 * 
 * TODO hook into War task to ensure correct packaging	
 * TODO Create GripesException class for handling sequence errors
 * FIXME Addons should install automatically using the `addon` configuration
 */
class GripesPlugin implements Plugin<Project> {
	Logger logger = LoggerFactory.getLogger(GripesPlugin.class)
	
	String root
	String basePackage
	String tmpDir
	
	GripesPlugin() {}
	
	/**
	 * Create the tasks for the plugin
	 */
	def void apply(Project project) {
		project.convention.plugins.gripes = new GripesPluginConvention()
		
		project.configurations.add "addons"
		
        def gripesConvention = (GripesPluginConvention) project.getConvention().getPlugins().get("gripes")
		def javaConvention = (JavaPluginConvention) project.getConvention().getPlugins().get("java")
		
		def resourcesDir = javaConvention.sourceSets.test.resources.srcDirs.iterator()[0]

		def deleteTask = project.task('delete') << {
			GripesCreate creator = new GripesCreate([project: project])
			creator.delete()
		}
		
		def initTask = project.task('init') << {
//			println "Base: " + gripesConvention
			def haveSourceFolder = project.sourceSets.main.groovy.srcDirs.find{it.exists()}
//			println "SrcFlder: " + haveSourceFolder
//			String baseDirString = gripesConvention.base.canonicalPath
//			if(new File("${baseDirString}/src").exists()){
			if(haveSourceFolder){
				throw new IllegalStateException("GripesSequenceError: `gradle init` has already been run.")
				return null
			} 
			GripesCreate creator = new GripesCreate([project: project])
			creator.init()
		}
		
		def setupTask = project.task('setup') << {
//			String baseDirString = gripesConvention.base.canonicalPath
			String baseDirString = project.projectDir.canonicalPath
			
			if(new File("${baseDirString}/src").exists()) {
				if(new File("${baseDirString}/src/META-INF").exists()){
					throw new IllegalStateException("GripesSequenceError: `gradle setup` has already been run.")
					return null
				}
				GripesCreate creator = new GripesCreate([project: project])
				creator.setup()
			} else {
				throw new IllegalStateException("GripesSequenceError: You must first run `gradle init`.")
				return null
			}
		}
		
		def createTask = project.task('create') << {
//			String baseDirString = gripesConvention.base.canonicalPath
			String baseDirString = project.projectDir.canonicalPath
			
//			println "Config:  " + "${baseDirString}/resources/Config.groovy"
			if(!(new File("${baseDirString}/resources/Config.groovy").exists())) {
				throw new IllegalStateException("GripesSequenceError: You must first run `gradle init` and `gradle setup`.")
				return null
			}
			GripesCreate creator = new GripesCreate([project: project])

			if(project.hasProperty('model')) {
				creator.model(project.properties.model,project.properties.package?:null)
			} else if (project.hasProperty('action')) {
				creator.action(project.properties.action,project.properties.package?:null)
			} else if (project.hasProperty('views')) {
				creator.views(project.properties.views,project.properties.package?:null)
			}
		}
		createTask.dependsOn<<project.compileGroovy

		/**
		 * Runs the Gripes application using the built-in Gradle Jetty 
		 * implementation. 
		 */
		def runTask = project.task('run') << {task->
			if(!(new File("resources/Config.groovy").exists())) {
				throw new IllegalStateException("GripesSequenceError: You must first run `gradle init` and `gradle setup`.")
				return null
			}
						
			def serverType = project.convention.plugins.gripes.server.type
			def server
			if(serverType.equals('tomcat')) {
				server = new GripesTomcat()
			} else {
				server = new GripesJetty()
				server.webAppSourceDirectory = new File(project.projectDir.canonicalPath+"/web")
			}
			server.project = project			
			server.start()
		}
		runTask.dependsOn<<project.clean
		runTask.dependsOn<<project.compileGroovy
		runTask.configure {
			def configFile = new File("resources/Config.groovy")
			
			if(configFile.exists()) {
				def gripesConfig = new ConfigSlurper().parse(configFile.text)
//				project.configurations.addons.each {
//					println "Addons: " + it
//				}  
//				gripesConfig.addons.each {
//					project.sourceSets.main.groovy.srcDirs +=
//					project.sourceSets.main.groovy.srcDirs += findAddon(it, project)
//					println "Adding: gripes-addons/${it.replaceAll('-src','')}/src/main/groovy to the sourceSet"
//					project.sourceSets.main.groovy.srcDirs += new File("gripes-addons/${it.replaceAll('-src','')}/src/main/groovy")
//				}
			}
		}
		
		def stopTask = project.task('stop') << {
			def serverType = project.convention.plugins.gripes.server.type
			
			def server
			if(serverType.equals('tomcat')) {
				server = new GripesTomcat()
				server.stop()
			} else {			
				project.convention.plugins.gripes.server.each { k, v ->
					project.jettyStop[k] = v
				}
				project.jettyStop.execute()
			}
		}
		
		/*
		 * FIXME This should be able to run automatically using the `addon` configuration and check for uninstalled
		 */
		def installTask = project.task('install') << {
			String baseDirString = project.projectDir.canonicalPath

			if(!(new File(baseDirString+"/resources/Config.groovy").exists())) {
				throw new IllegalStateException("GripesSequenceError: You must first run `gradle init` and `gradle setup`.")
				return null
			}
			
			GripesCreate creator = new GripesCreate([project: project])
			if(project.properties.dir) {
				logger.debug "Installing addon[${project.properties.addon}] to {}", project.properties.dir
				creator.install(project.properties.addon, project.properties.dir) 
			} else {
				logger.debug "Installing addon[${project.properties.addon}] to default standard location"
				creator.install(project.properties.addon)
			}
		}
		installTask.doFirst {
			logger.info "Installing an addon..."
		}
		
		def warTask = project.getTasks().getByPath("war")
		warTask.onlyIf {			
			def tempDir = new File(warTask.getTemporaryDir().canonicalPath+"/classes/META-INF")
			if(!tempDir.exists()){
				tempDir.mkdirs()
			}
			
			def dbConfig, mainConfig
			try {
				dbConfig = new ConfigSlurper().parse(new File('resources/DB.groovy').toURI().toURL())
				mainConfig = new ConfigSlurper().parse(new File('resources/Config.groovy').toURI().toURL())	
			
				def addons = mainConfig.addons
			
				def jpaFile = new File(tempDir.canonicalPath+"/persistence.xml")
				jpaFile.createNewFile()
				jpaFile.text = GripesUtil.createJpaFile(dbConfig, mainConfig.addons)

				def gripesProps = new Properties()
				new File("conf/gripes.properties").withInputStream { 
				  stream -> gripesProps.load(stream) 
				}

				def webXmlTemplate = getResource("web.xml").text
				def webXml = new File(warTask.getTemporaryDir().canonicalPath+"/web.xml")
				webXml.createNewFile()
				webXml.text = webXmlTemplate
								.replaceAll("ACTIONPACKAGES", gripesProps["actions"])
								.replaceAll("PROJECTNAME",GripesUtil.getSettings(project).appName)
								.replaceAll("PACKAGE",GripesUtil.getSettings(project).packageBase)
				
				warTask.webInf { 
					from(GripesUtil.getSettings(project).server.webAppSourceDirectory.name+"/WEB-INF")
					from(warTask.getTemporaryDir().canonicalPath) {
						exclude('MANIFEST.MF')
					}
				}
				warTask.from { GripesUtil.getSettings(project).server.webAppSourceDirectory.name }
				
				true
			} catch (e) {
				e.printStackTrace()
				false
			}
		}
    }
	
	private def findAddon(addon, project) {
		def srcDir
		
		if(new File("gripes-addons/${addon.replaceAll('-src','')}/src").exists()){
			srcDir = new File("gripes-addons/${addon.replaceAll('-src','')}/src")
		} else if(new File("../${addon.replaceAll('-src','')}/src").exists()) {
			srcDir = new File("../${addon.replaceAll('-src','')}/src")
		}
		
		srcDir		
	}

	private def copyWebXml(project) {
	    def webXmlText = getResource("web.xml").text
 		def webXml = new File(GripesUtil.getTempDir(project).canonicalPath+"/web.xml")
		webXml.createNewFile()
		webXml.deleteOnExit()
		webXml.text = webXmlText.replaceAll("PACKAGE",GripesUtil.getSettings(project).packageBase)
		
		webXml
	}
	
	private def getResource(resource) {
		getClass().classLoader.getResource(resource)
	}
}