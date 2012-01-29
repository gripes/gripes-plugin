package net.sf.gripes

import net.sourceforge.stripes.action.*
import net.sourceforge.stripes.util.*

class GripesTomcat {
	def project
	def webAppSourceDirectory
	def httpPort = 8888
	def stopPort = 8889
	def stopKey  = "stopJetty"
	
	def start() {
		[
			"webAppSourceDirectory",
			"httpPort",
			"stopPort",
			"stopKey",
			"contextPath"
		].each {
			project.tomcatRun[it] = GripesUtil.getSettings(project).server."$it"
		}
		
		def dbConfig = new ConfigSlurper().parse(new File('resources/DB.groovy').toURI().toURL())
		def gripesConfig = new ConfigSlurper().parse(new File('resources/Config.groovy').toURI().toURL())
						
		[
			new File("build/classes/main/META-INF/"),
			new File(project.tomcatRun['webAppSourceDirectory'].canonicalPath+"/WEB-INF/classes/gripes/addons")
		].each {
			it.mkdirs()
			it.deleteOnExit()
		}
		
		def jpaFile = new File("build/classes/main/META-INF/persistence.xml")
		jpaFile.createNewFile()
		jpaFile.deleteOnExit()
		jpaFile.text = GripesUtil.createJpaFile(dbConfig, gripesConfig.addons)
		
		def gripesProps = new Properties()
		new File("conf/gripes.properties").withInputStream {
		  stream -> gripesProps.load(stream)
		}
		
		[
			"import.groovy",
			"StripesResources.properties",
			"DB.groovy",
			"logback.groovy",
			"Config.groovy"
		].each {
			def newFile = new File(project.tomcatRun['webAppSourceDirectory'].canonicalPath+"/WEB-INF/classes/${it}")
			newFile.createNewFile()
			newFile.deleteOnExit()
			newFile.text = new File(GripesUtil.getResourceDir(project)+"/${it}").text
		}
		
		def webXmlTemplate = getResource("web.xml").text
		def webXml = new File(project.tomcatRun['webAppSourceDirectory'].canonicalPath+"/WEB-INF/web.xml")
		webXml.createNewFile()
		webXml.deleteOnExit()
		webXml.text = webXmlTemplate
						.replaceAll("ACTIONPACKAGES", gripesProps["actions"]?:GripesUtil.getSettings(project).packageBase+".action")
						.replaceAll("PROJECTNAME",GripesUtil.getSettings(project).appName)
						.replaceAll("PACKAGE",GripesUtil.getSettings(project).packageBase)
		
		project.tomcatRun.execute()
//		println "STARTING TOMCAT"
//		
//		def ant = new AntBuilder()
//		def dbConfig = new ConfigSlurper().parse(new File('resources/DB.groovy').toURI().toURL())
//		def gripesConfig = new ConfigSlurper().parse(new File('resources/Config.groovy').toURI().toURL())
//						
//		[
//			new File("build/classes/main/META-INF/"),
//			new File(webAppSourceDirectory.canonicalPath+"/WEB-INF/classes/gripes/addons")
//		].each {
//			it.mkdirs()
//			it.deleteOnExit()
//		}
//		
//		def jpaFile = new File("build/classes/main/META-INF/persistence.xml")
//		jpaFile.createNewFile()
//		jpaFile.deleteOnExit()
//		jpaFile.text = GripesUtil.createJpaFile(dbConfig, gripesConfig.addons)
//		
//		[
//			"import.groovy",
//			"StripesResources.properties",
//			"DB.groovy",
//			"logback.groovy",
//			"Config.groovy"
//		].each {
//			def newFile = new File(webAppSourceDirectory.canonicalPath+"/WEB-INF/classes/${it}")
//			newFile.createNewFile()
//			newFile.deleteOnExit()
//			newFile.text = new File(GripesUtil.getResourceDir(project)+"/${it}").text
//		}
//		
//		
//		def gripesProps = new Properties()
//		new File("conf/gripes.properties").withInputStream { 
//		  stream -> gripesProps.load(stream) 
//		}
//		
//		def webXmlTemplate = getResource("web.xml").text
//		def webXml = new File(webAppSourceDirectory.canonicalPath+"/WEB-INF/web.xml")
//		webXml.createNewFile()
//		webXml.deleteOnExit()
//		webXml.text = webXmlTemplate
//						.replaceAll("ACTIONPACKAGES", gripesProps["actions"]?:GripesUtil.getSettings(project).packageBase+".action")
//						.replaceAll("PROJECTNAME",GripesUtil.getSettings(project).appName)
//						.replaceAll("PACKAGE",GripesUtil.getSettings(project).packageBase)
//		
//		def server = new GripesTomcatServer("/", 8888, "/", true, project)
//		
//		println "SERVER: " + server
//		server.start()
//		println server.isRunning
	}
	
	def stop() {
//		def server = new GripesTomcatServer("/", 8888, "/", true)
//		server.stop()
	}

	private def getResource(resource) {
		getClass().classLoader.getResource(resource)
	}
}