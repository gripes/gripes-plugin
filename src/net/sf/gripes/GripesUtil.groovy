package net.sf.gripes

class GripesUtil {
	static def getRoot(project) {
		project.projectDir.canonicalPath
	}
	static def getTempDir(project) {
		def tmp = new File(project.buildDir.canonicalPath+"/tmp/gripes")
		if(!tmp.exists()) tmp.mkdirs()
		tmp
	}
	static def getSettings(project) {
		project.convention.plugins.gripes
	}

	static def getSourceDir(project) {
//		this.getRoot(project)
		this.getSettings(project).src
	}
	
	static def getResourceDir(project) {
		def gripesRes = this.getSettings(project).resources

		project.projectDir.canonicalPath+gripesRes
	}

	static def getBasePackage(project) {
//		this.getRoot(project)
		this.getSettings(project).src+"/"+this.getSettings(project).packageBase.replace(".","/")
	}
	
	static def packageToDir(project, pkg) {
//		this.getRoot(project)+
		this.getSettings(project).src+"/"+pkg.replace(".","/")
	}
	
	static def makeDir(parentFile) {
		if(!parentFile.exists()){
			parentFile.mkdirs()
		}
	}
	
	static def saveFile(file,template) {		
		makeDir(file.parentFile)
		if(!file.exists()) {
			file.createNewFile()
			file.text = template
		}
	}
	
	static String createJpaFile(dbConfig, addons){
		def jpaTemplate = """
<persistence xmlns="http://java.sun.com/xml/ns/persistence"
	xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
	xsi:schemaLocation="http://java.sun.com/xml/ns/persistence http://java.sun.com/xml/ns/persistence/persistence_2_0.xsd" 
	version="2.0">
		"""
		
		dbConfig.database.each { k,v ->
			jpaTemplate += this.classLoader.getResource("conf/persistence.template").text
			jpaTemplate = jpaTemplate
							.replaceAll(/\[NAME\]/,k)
							.replaceAll(/\[DBSCHEMA\]/,v.schema)
							.replaceAll(/\[DBDIALECT\]/,v.dialect)
							.replaceAll(/\[DBDRIVER\]/,v.driver)
							.replaceAll(/\[DBURL\]/,v.url)
							.replaceAll(/\[DBUSER\]/,v.user)
							.replaceAll(/\[DBPASSWORD\]/,v.password)

		    if(v.classes.equals("auto")) {
				jpaTemplate = jpaTemplate
								.replaceAll(/\[AUTO\]/,'<property name="hibernate.archive.autodetection" value="class"/>')
								.replaceAll(/\[CLASSES\]/,'')
			} else {
				jpaTemplate = jpaTemplate
								.replaceAll(/\[AUTO\]/,'')
								.replaceAll(/\[CLASSES\]/, v.classes.collect{"<class>$it</class>"}.join("\n"))
			}
		}
		
		
		def addonConfig
		if(addons.size()>0) {
			addons.each { addon ->
//				println "Adding: ${addon}"
				if((addon=~/-src/).find()) {
					addonConfig = new File("gripes-addons/"+addon.replace("-src","")+"/gripes.addon")
				} else if(new File("addons/${addon}/gripes.addon").exists()) {
					addonConfig = new File("addons/${addon}/gripes.addon")
				} else {
					addonConfig = new File("../${addon}/gripes.addon")
				}
	
				def config = new ConfigSlurper().parse(addonConfig.text)
				jpaTemplate = jpaTemplate.replaceAll(/\[ADDITIONAL\]/,"[ADDITIONAL]"+((config.persistence.size()>0)?config.persistence:''))
			}
		} else {
			jpaTemplate = jpaTemplate.replaceAll(/\[ADDITIONAL\]/,"")
		}
		jpaTemplate += "\n</persistence>\n"
		jpaTemplate
	}
}