package net.sf.gripes

import javax.persistence.Column

import java.lang.reflect.Field
import java.lang.reflect.Modifier;

import org.slf4j.Logger
import org.slf4j.LoggerFactory

/**
 * 
 * @author clmarquart
 */
class GripesCreate {
	Logger logger = LoggerFactory.getLogger(GripesCreate.class)
	def project
	def urlLoader
	
	/**
	 * Tear down the Gripes application. 
	 *
	 * TODO: Probably don't need this in later versions.  Nice for testing app creation.
	 */
	def delete() {
		logger.info "Tearing down the Gripes project"
		
//		println "PROJ DIR: " + 
//		String baseDirString = ((GripesPluginConvention) getPluginConvention("gripes")).base.canonicalPath
		String baseDirString = project.projectDir.canonicalPath
		new AntBuilder().sequential {
			delete(dir: "${baseDirString}/conf")
			delete(dir: "${baseDirString}/src")
			delete(dir: "${baseDirString}/resources")
			delete(dir: "${baseDirString}/web")
			delete(dir: "${baseDirString}/addons")
		}
	}
	
	/**
	 * Initializes a directory as a Gripes application.  Creates the folder structure,
	 * gripes-basic` gradle script, and edits the build.gradle to use gripes-basic
	 * 
	 * FIXME This needs to use a property for the base folder directory, primarily for testing
	 */
	def init() {
//		String baseDirString = ((GripesPluginConvention) getPluginConvention("gripes")).base.canonicalPath
		String baseDirString = project.projectDir.canonicalPath
		
		logger.info "Initializing Gripes project"
		
		['/src','/resources','/web','/conf'].each {
			def dir = new File(baseDirString+"${it}")
			if(!dir.exists()) {
				dir.mkdirs()
			}
		}
		
		// TODO Config.groovy needs to create action list by default
		['conf/gripes-basic.gradle','conf/gripes.properties','resources/Config.groovy'].each {
			def newFile = new File(baseDirString+"/"+it)
			if(!newFile.exists()) {
				newFile.createNewFile()
				def tempFile = getResource(it)
				if(tempFile) {
					newFile.text = tempFile.text
				}
			}
		}
	}
	
	/**
	 * Called after the application has been initialized.  Creates the necessary 
	 * packages and base models, from the gripes{} settings in the build script.
	 *
	 * TODO: Restructure the copying of files, seems rather messy and could be cleaned up
	 */ 
	def setup() {
		logger.info "Setting up Gripes project"
		
//		String baseDirString = ((GripesPluginConvention) getPluginConvention("gripes")).base.canonicalPath
		String baseDirString = project.projectDir.canonicalPath
		
		[
			'resources/DB.groovy','resources/Config.groovy',
			'resources/StripesResources.properties',
			'resources/logback.groovy',
			'web/index.jsp'
		].each {
			def newFile = new File(baseDirString+"/${it}")
			if(!newFile.exists()) {
				newFile.createNewFile()
				def tempFile = getResource("${it}")
				logger.debug "Creating: ${newFile} from ${tempFile}"
				
				if(tempFile) {
					newFile.text = tempFile.text.replace("PACKAGE",GripesUtil.getSettings(project).packageBase)
				}	
			}
		}
		
		// This is the META-INF for the persistence.xml file in the source folder
		['META-INF'].each {
			def dir = new File(baseDirString+GripesUtil.getSourceDir(project)+"/${it}")
			if(!dir.exists()){
				dir.mkdirs()
			}
		}
		
		['action/base','model/base','util','dao/base'].each {
			def dir = new File(baseDirString+GripesUtil.getBasePackage(project)+"/${it}")
			if(!dir.exists()){
				dir.mkdirs()
			}
		}
		
		[
		 'style',
		 'script',
		 'images',
		 'WEB-INF/jsp/layout',
		 'WEB-INF/jsp/includes',
		 'WEB-INF/classes',
		 'WEB-INF/work',
		 'META-INF'
		].each {
//			def dir = new File(GripesUtil.getRoot(project)+"/web/${it}")
			def dir = new File(baseDirString+"/web/${it}")
			if(!dir.exists()){
				dir.mkdirs()
			}
		}
		
		// Reference for file creation
//		def dir = new File(GripesUtil.getRoot(project)+"/web/WEB-INF/jsp/")
		def dir = new File(baseDirString+"/web/WEB-INF/jsp/")
		[
			"layout/main.jsp",
			"includes/taglibs.jsp",
			"includes/_adminBar.jsp"
		].each {
			saveFile(new File(dir.canonicalPath+"/${it}"),getResource("templates/jsp/${it}").text)
		}
		
		[
			"web/style/main.css",
			"web/script/jquery.js",
			"web/script/main.js",
			"resources/StripesResources.properties",
			"resources/import.groovy"
		].each {
			saveFile(new File(baseDirString+"/${it}"),getResource(it).text)
		}
		
		[
			"dao/base/BaseDao.groovy",
			"action/base/BaseActionBean.groovy",
			"util/base/BaseActionBeanContext.groovy",
			"model/base/BaseModel.groovy"
		].each {
			saveFile(
				new File(baseDirString+GripesUtil.getBasePackage(project)+"/${it}"),
				getResource	("templates/${it}").text
					.replaceAll("PACKAGE",GripesUtil.getSettings(project).packageBase)
					.replaceAll("PKG","PACKAGE")
			)
		}
/*		
		saveFile(
			new File(GripesUtil.getRoot(project)+"/web/WEB-INF/web.xml"),
			getResource("web.xml").text.replaceAll("PACKAGE",GripesUtil.getSettings(project).packageBase)
		)
*/
	}
	
	/**
	 * Creates a JPA Entity model object
	 * 
	 * TODO Check for packageBase from command line, then use default
	 * FIXME Fix GripesUtil call to get pkg folder, need to use cmd line
	 */
	def model(name, pkg) {
		logger.info "Creating {} Model in Package: {}", name, pkg
/*		GripesUtil.getSettings(project).packageBase = pkg?:GripesUtil.getSettings(project).packageBase*/
		
		def file, template

		template = getResource("templates/model/Model.template").text
						.replaceAll("MODEL",name)	
						.replaceAll("BASEPACKAGE",GripesUtil.getSettings(project).packageBase)
						.replaceAll("PACKAGE",pkg?:GripesUtil.getSettings(project).packageBase)
		file = new File(GripesUtil.packageToDir(project,pkg?:GripesUtil.getSettings(project).packageBase)+"/model/${name}.groovy")
		saveFile(file, template)
		
		template = getResource("templates/dao/Dao.template").text
						.replaceAll("MODEL",name)
						.replaceAll("BASEPACKAGE",GripesUtil.getSettings(project).packageBase)
						.replaceAll("PACKAGE",pkg?:GripesUtil.getSettings(project).packageBase)
		file = new File(GripesUtil.packageToDir(project,pkg?:GripesUtil.getSettings(project).packageBase)+"/dao/${name}Dao.groovy")		
		saveFile(file,template)
	}
	
	/**
	 * Creates a Stripes ActionBean for a previously created Model
	 *
	 * TODO Check for packageBase from command line, then use default
	 * 
	 * @param name String representing the name of the model to create the ActionBean from
	 * @param pkg String representing the base package to put the new ActionBean in
	 * 
	 * @author clmarquart
	 */
	def action(name, pkg) {
		logger.info "Creating {} ActionBean in Package: {}", name, pkg
/*		GripesUtil.getSettings(project).packageBase = pkg?:GripesUtil.getSettings(project).packageBase*/
		
		def file, template
		
		template =  getResource("templates/action/ActionBean.template").text
						.replaceAll("MODELL",name.toLowerCase())
						.replaceAll("MODEL_FIELD",name+" "+name.toLowerCase())
						.replaceAll("MODEL",name)
						.replaceAll("BASEPACKAGE",GripesUtil.getSettings(project).packageBase)
						.replaceAll("PACKAGE",pkg?:GripesUtil.getSettings(project).packageBase)
						
		file = new File(GripesUtil.packageToDir(project,pkg?:GripesUtil.getSettings(project).packageBase)+"/action/${name}ActionBean.groovy")
		saveFile(file, template)
		
		
		def gripesProps = new Properties()
		new File("conf/gripes.properties").withInputStream { 
		  stream -> gripesProps.load(stream) 
		}
		
		if(gripesProps["actions"] && (gripesProps["actions"].indexOf("${(pkg?:GripesUtil.getSettings(project).packageBase)}.action") < 0))
			gripesProps["actions"] = (gripesProps['actions']+","+(pkg?:GripesUtil.getSettings(project).packageBase)+".action")
		else if (!gripesProps["actions"])
			gripesProps["actions"] = (pkg?:GripesUtil.getSettings(project).packageBase)+".action"
			
		gripesProps.store(new FileOutputStream(new File("conf/gripes.properties")), null)
		
		urlLoader = (URLClassLoader) this.class.classLoader
		urlLoader.addURL(new File("build/classes/main/").toURI().toURL())
		urlLoader.addURL(new File("gripes-web/build/classes/main/").toURI().toURL())
		
		createViews(name, urlLoader.findClass("${pkg?:GripesUtil.getSettings(project).packageBase}.model.${name}"), pkg)	
	}
	
	/**
	 * Creates the views for an ActionBean.  Can be called directly if the views need
	 * to be recreated.
	 */
	def views(name,pkg) {
//		logger.info "Creating Views for the {}ActionBean in Package: {}", name, pkg
		GripesUtil.getSettings(project).packageBase = pkg?:GripesUtil.getSettings(project).packageBase
		
		urlLoader = (URLClassLoader) this.class.classLoader
		urlLoader.addURL(new File("build/classes/main/").toURI().toURL())				
		
		createViews(name, urlLoader.findClass("${GripesUtil.getSettings(project).packageBase}.model.${name}"), pkg)
	}
	
	/**  
	 * Install the specified addon to the provided directory
	 * 
	 * @param addon String name of the addon being installed
	 * @param dir String directory to search the addon for
	 */
	def install(String addon, String dir) {
		logger.info "Installing the {} add-on to {}.", addon, dir
		
		String baseDirString = gripesConvention.base.canonicalPath
		def gripesConfigFile = getGripesConfigFile()
		def currentConfig = new ConfigSlurper().parse(gripesConfigFile.text)
		
		def addonConfig, installScript = "", installScriptFile
		if(!hasAddon(addon)){
			addonConfig = new ConfigSlurper().parse(new File("${dir}/${addon}/gripes.addon").text)
		
			installScriptFile = new File("${baseDirString}/${dir}/${addon}/gripes.install")
		
			if(installScriptFile.exists()) 
				installScript = installScriptFile.text
			
			installAddon(addon, installScriptFile.parentFile, installScript, gripesConfigFile)
		}
	}
	
	/**
	 * Install the specified addon to the default directory
	 * 
	 * @param addon String name of the addon being installed
	 */
	def install(String addon) {
        def gripesConvention = (GripesPluginConvention) project.getConvention().getPlugins().get("gripes")
		
//		String baseDirString = gripesConvention.base.canonicalPath
		String baseDirString = project.projectDir.canonicalPath
		
		def addonName = addon
		logger.info "Installing the {} add-on.", addon
		
//		String baseDirString = gripesConvention.base.canonicalPath
		def gripesConfigFile = getGripesConfigFile()
		def gripesConfig = gripesConfigFile.text
		def currentConfig = new ConfigSlurper().parse(gripesConfig)
		
		if(!hasAddon(addon)) {
			def addonConfig, 			// Slurped Config file
				addonDir,
				addonInstallName = "", 	// Name used for finding local addon directory 
				installScriptFile, 		// Install script file
				installScript = ""; 	// String contents of the installScriptFile
				
			// If we are installing a plugin from a local source folder...
			if((addon=~/-src/).find()){
				addon = addon.replaceFirst(/-src/,'')
				addonConfig = new ConfigSlurper().parse(new File("gripes-addons/${addon}/gripes.addon").text)
			
				installScriptFile = new File("gripes-addons/${addon}/gripes.install")
							
			// Else we need to try and use the `addon` configuration
			} else {
				//TODO the jar should be downloaded now add it to the current classpath and continue
				URL[] myurl = [];
				URLClassLoader urlLoader = new URLClassLoader(myurl, this.class.classLoader)
				println "ADDONS: " + project.configurations.addons
				project.configurations.addons.each { File file ->
					println "Adding jar to loader: " + file.toURI().toURL()
					urlLoader.addURL(file.toURI().toURL())
				}
				
//				addonDir = makeDir(new File("${baseDirString}/addons/${addon}"))
//				File addonJar = new File(addonDir.canonicalPath+"/bin/${addon}.jar")
				
				if (!urlLoader.findResource("gripes.install")) {
					logger.info "gripes.install for the addon ${addon} is missing"
					throw new MissingResourceException("gripes.install for the addon ${addon} is missing")
					return null
				}
			
				installScriptFile = urlLoader.findResource("gripes.install")
				
				logger.info "InstallScript: {}", installScriptFile				
			}

			installAddon(addon, addonDir, installScriptFile.text, gripesConfigFile)
		} else {
			logger.info "The $addon addon is already installed."
		}
	}
	
	private boolean hasAddon(addon) {
		new ConfigSlurper().parse(getGripesConfigFile().text).addons.find{it==addon}
	}
	
	public File getGripesConfigFile() {
		def gripesConvention = (GripesPluginConvention) project.getConvention().getPlugins().get("gripes")
//		String baseDirString = gripesConvention.base.canonicalPath
		String baseDirString = project.projectDir.canonicalPath
		
		new File("${baseDirString}/resources/Config.groovy")
	}
	
	// FIXME Somehow need to get the proper jar from the classpath configuration for the addons
	//  maybe add the project.configurations.addon.runtime
	private def installAddon(addonName, addonDir, installScript, gripesConfigFile) {
		println "Installing addon: ${installScript}"
		
//		String baseDirString = gripesConvention.base.canonicalPath
		String baseDirString = project.projectDir.canonicalPath
		
        def gripesConvention = (GripesPluginConvention) project.getConvention().getPlugins().get("gripes")
			
		def gripesConfig = gripesConfigFile.text
		
		if(installScript!=""){
			logger.info "Executing the ${addonName} install script"
			
//			URLClassLoader childLoader = new URLClassLoader ([new File("${baseDirString}addons/${addonName}/bin/${addonName}.jar").toURI().toURL()] as URL[], this.class.classLoader);
			
			URLClassLoader childLoader = new URLClassLoader ([] as URL[], this.class.classLoader);
		
			new GroovyShell(childLoader,new Binding([project: project, addonDir: addonDir])).evaluate(installScript)
		}
	
		/*
		 * Place the addon name in the Config file, marked as installed. 
		 */
		if((gripesConfig =~ /addons\s*=\s*\[\]/).find()) {
			gripesConfig = gripesConfig.replaceFirst(/addons\s*=\s*\[\s*\]/,'addons=["'+addonName+'"]')
		} else {
			gripesConfig = gripesConfig.replaceFirst(/addons\s*=\s*\[/,'addons=["'+addonName+'",')
		}
		gripesConfigFile.text = gripesConfig
	}
	
	// FIXME Need to compensate for proper directory.
	private def download(addon) {
        def gripesConvention = (GripesPluginConvention) project.getConvention().getPlugins().get("gripes")
		String baseDirString = gripesConvention.base.canonicalPath
		
		[
		 "${baseDirString}/addons/${addon}/bin"
		].each {
			def dir = new File("${it}")
			if(!dir.exists()) dir.mkdirs()
		}
		
		def fos, out
		FileOutputStream file
		try {
			["gripes.addon","gripes.install"].each {
				def location = "${baseDirString}/addons/${addon}/${it}"
				try {
				    fos = new FileOutputStream(location)
				    out = new BufferedOutputStream(fos)
				    out << new URL("http://www.gripes-project.org/addons/${addon}/${it}").openStream()
				    out.close()	
				} catch (e) { 
					logger.warn e.getMessage()
					logger.warn "Remote file ($it) doesn't exist."
					new File(location).text = ""
				}
			}
		} catch (e) { 
			e.printStackTrace()
		}
	
	    file = new FileOutputStream("${baseDirString}/addons/${addon}/bin/${addon}.jar")
	    out = new BufferedOutputStream(file)
	    out << new URL("http://www.gripes-project.org/addons/${addon}/bin/${addon}.jar").openStream()
	    out.close()
		
		return (new File("${baseDirString}/addons/${addon}/bin/${addon}.jar"))
	}
	
	private File makeDir(File parentFile) {
		if(!parentFile.exists()){
			parentFile.mkdirs()
		}
		parentFile
	}
	
	private def saveFile(file,template) {
		logger.info "Saving {}", file
		logger.info "setting text: {}", template.length()
		
		makeDir(file.parentFile)
		if(!file.exists()) {
			file.createNewFile()
			file.text = template
		}
	}

	private def createViews(action, model,pkg) {
		def fields = model.declaredFields.findAll { Field f -> 
			(!f.isSynthetic() && !Modifier.isStatic(f.getModifiers()) && !f.name.equals('mappings')) 
		} 
		logger.debug "FIELDS: {}", fields
		
		[new File(GripesUtil.getRoot(project)+"/web/WEB-INF/jsp/${action.toLowerCase()}")].each{
			if(!it.exists()){it.mkdirs()}
		}
		
		def jspdir = new File(GripesUtil.getRoot(project)+"/web/WEB-INF/jsp/${action.toLowerCase()}")
		
		["view"].each {
			def file = new File(jspdir.canonicalPath+"/${it}.jsp")
			def template = getResource("templates/jsp/${it}.template").text
			def newContents = ""
			fields.each {
				newContents+=template.replace("LABEL",it.name)
								.replace("VALUE",getViewValue(model,it))
								/*								
								.replace("TYPE",getFieldType(it))
								.replace("MODEL",model.simpleName)
								*/
			}			
			file.createNewFile()
			file.text =  createJspTemplate(model,newContents,"list,create","View", pkg)
		}
		
		["edit","create"].each {
			def file = new File(jspdir.canonicalPath+"/${it}.jsp")
			def template = getResource("templates/jsp/${it}.template").text
			def newContents = ""			
			fields.each {
				newContents+=template.replace("LABEL",it.name)
								.replace("VALUE",'${bean.'+model.simpleName.toLowerCase()+"."+it.name+'}')
								.replace("TYPE",getFieldType(it))
								.replace("MODEL",model.simpleName.toLowerCase())
								.replace("INPUT",createInputField(model,it))
			}
			newContents =  """
	<stripes:form beanclass="${pkg?:GripesUtil.getSettings(project).packageBase}.action.${model.simpleName}ActionBean">
		${newContents}
"""			
			newContents+= '<stripes:hidden name="'+model.simpleName.toLowerCase()+'" value="${bean.'+model.simpleName.toLowerCase()+'.id}" />'
			newContents+= """
		<stripes:submit name="save" value="Save" />
	</stripes:form>
"""			
			file.createNewFile()
			file.text =  createJspTemplate(model,newContents,"list",it, pkg)
		}
		
		["list"].each {
			def newContents = ""
			def file = new File(jspdir.canonicalPath+"/${it}.jsp")
			def template = getResource("templates/jsp/${it}.template").text
			
			newContents += template
							.replace("MODEL",model.simpleName)
							.replace("BEANLIST",'${'+"requestScope['list']"+'}')
							.replace("LISTHEADER",getTableHeader(fields))
							.replace("LISTENTRY",getTableRow(fields,model))
			def layout = createJspTemplate(model,newContents,"create","List", pkg)
			file.createNewFile()
			file.text = layout
		}
	}
	
	private def getViewValue(model,field) {
		def html = ""
		
		if(isEntity(field)) {
			if(isCollection(field)){
				html += '<c:forEach items="${bean.'+model.simpleName.toLowerCase()+"."+field.name+'}" var="it">\n'
				html += '\t<stripes:link href="/${fn:toLowerCase(it.class.simpleName)}/view?${fn:toLowerCase(it.class.simpleName)}=${it.id}">${it}</stripes:link>\n'
				html += '</c:forEach>\n'
			} else {
				html += '\t<stripes:link href="/'+field.type.simpleName.toLowerCase()+'/view?'+field.type.simpleName.toLowerCase()+'=${bean.'+model.simpleName.toLowerCase()+'.'+field.name+'.id}">${bean.'+model.simpleName.toLowerCase()+'.'+field.name+'}</stripes:link>\n'	
			}
		} else {	
			html += '${bean.'+model.simpleName.toLowerCase()+"."+field.name+'}'
		}
		html
	}
	
	private def createInputField(model,field) {	
		def html=""
		if(!field.type.isPrimitive()){
			if(field.type.equals(String) || (field.type.superclass.equals(Number))) {
				html = '<input type="text" value="${bean.'+model.simpleName.toLowerCase()+'.'+field.name+'}" name="'+model.simpleName.toLowerCase()+'.'+field.name+'" />'
			} else if(field.type.interfaces.find{it.equals(java.util.Collection)}) {
				html += '<c:forEach items="${bean.'+model.simpleName.toLowerCase()+"."+field.name+'}" var="it">\n'
				html += '\t<stripes:link href="/${fn:toLowerCase(it.class.simpleName)}/view?${it.class.simpleName}=${it.id}">${it}</stripes:link>\n'
				html += '</c:forEach>\n'
			} else if(field.type.getAnnotation(javax.persistence.Entity)) {
				html += '<c:choose>\n'
				html += '\t<c:when test="${bean.'+model.simpleName.toLowerCase()+"."+field.name+'}">\n'
				html += '\t\t<stripes:link href="/'+field.type.simpleName.toLowerCase()+'/edit?'+field.type.simpleName.toLowerCase()+'=${bean.'+model.simpleName.toLowerCase()+'.'+field.name+'.id}">Link</stripes:link>\n'
				html += '\t</c:when>\n'
				html += '\t<c:otherwise>\n'
				html += '\t\t<stripes:link href="/'+field.type.simpleName.toLowerCase()+'/create">Create</stripes:link>\n'
				html += '\t</c:otherwise>\n'
				html += '</c:choose>\n'
			}
		} else {
			if(field.type.equals(boolean)) {
				html = '<input type="checkbox" value="1" ${(bean.'+model.simpleName.toLowerCase()+'.'+field.name+'==true)?"checked=\'checked\'":""} name="'+model.simpleName.toLowerCase()+'.'+field.name+'" />'
			}
		}

		html
	}
	
	private def createJspTemplate(model,newContents,adminBar,action,pkg) {
		def str = """
<%@ include file="/WEB-INF/jsp/includes/taglibs.jsp" %>
<stripes:useActionBean id="bean" beanclass="${pkg?:GripesUtil.getSettings(project).packageBase}.action.${model.simpleName}ActionBean"/>
<stripes:layout-render name='../layout/main.jsp' pageTitle='${model.simpleName} ${action}'>
"""
		str +=  '\t<stripes:layout-component name="adminBar">\n'
		str += 	'\t\t<stripes:layout-render name="../includes/_adminBar.jsp" bean="${bean}" links="'+adminBar+'" />\n'
		str +=  '\t</stripes:layout-component>\n'
		str += """
	<stripes:layout-component name="content">
		${newContents}
	</stripes:layout-component>
</stripes:layout-render>
"""
		str
	}
	
	private def getTableRow(fields,model) {
		def header = "<tr>\n"
		fields.each {
			header += "\t\t\t\t<td>\n"
			if(!it.type.interfaces.find{it.equals(java.util.Collection)}){
				header += '\t\t\t\t\t${item.'+it.name+"}\n"	
			}
			header += "\t\t\t\t</td>\n"
		}
		header += "\t\t\t\t<td>\n"
		header += "\t\t\t\t\t<stripes:link href='/"+model.simpleName.toLowerCase()+"/view?"+model.simpleName.toLowerCase()+'=${item.id}'+"'>View</stripes:link>"
		header += "&nbsp;|&nbsp;"
		header += "\t\t\t\t\t<stripes:link href='/"+model.simpleName.toLowerCase()+"/edit?"+model.simpleName.toLowerCase()+'=${item.id}'+"'>Edit</stripes:link>"
		header += "&nbsp;|&nbsp;"
		header += "<stripes:link class='deleteObject' href='/"+model.simpleName.toLowerCase()+"/delete?"+model.simpleName.toLowerCase()+'=${item.id}'+"'>Delete</stripes:link>"
		header += "\n\t\t\t\t</td>\n\t\t\t</tr>"
	}
	
	private def getTableHeader(fields) {
		def header = "<tr>"
		fields.each {
			if(!it.type.interfaces.find{it.equals(java.util.Collection)}){
				header += "<th>${it.name}</th>"
			}
		}
		header += "</tr>"
	}

	private def getFieldType(field) {
		def baseModel = this.class.classLoader.findLoadedClass("${GripesUtil.getSettings(project).packageBase}.model.base.BaseModel")

		if(field.type.equals(String)){
			"text"
		} else if(field.type.equals(boolean)){
			"checkbox"
		} else if(field.type.getAnnotation(javax.persistence.Entity)) {
			"text"
		}
		"CHANGEME"
	}
	
	private def isCollection(field) { field.type.interfaces.find{it.equals(java.util.Collection)} }
	private def isEntity(field) { (field.type.getAnnotation(javax.persistence.Entity))?true:false }
	
	private def copyActionBeanContext(project) {
		def basePackage = GripesUtil.getRoot(project)+GripesUtil.getSettings(project).src+"/"+GripesUtil.getSettings(project).packageBase.replace(".","/")
		def template = getResource("templates/util/ActionBeanContext.template")
		template = template.text.replaceAll("PROJECTNAME",GripesUtil.getSettings(project).appName)
								.replaceAll("PACKAGE",GripesUtil.getSettings(project).packageBase)
		
		def newFile = new File(basePackage+"/util/base/BaseActionBeanContext.groovy")
		if(!newFile.exists()) {
			newFile.createNewFile()
			newFile.text = template
		}
	}
	
	private def copyJpaXml(project,config) {
	    def jpaXmlText = getResource("config/persistence.template").text
 		def jpaXml = new File(GripesUtil.getSourceDir(project)+"/META-INF/persistence.xml")
		if(!jpaXml.exists()){
			jpaXml.createNewFile()
			jpaXml.text = jpaXmlText.replaceAll("PACKAGE",GripesUtil.getSettings(project).packageBase)
			jpaXml	
		}
	}
	
	private def getPluginConvention(String plugin) {
		project.getConvention().getPlugins().get(plugin)
	}
	
	private def getResource(resource) {
		getClass().classLoader.getResource(resource)
	}
}