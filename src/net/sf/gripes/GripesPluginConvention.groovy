package net.sf.gripes

class GripesPluginConvention {
	String appName
	File base
	String src
	String resources
	String packageBase
	String httpPort
	String stopPort
	String webappSource
	Map server = [:]
	def addons

    def gripes(Closure closure) {
        closure.delegate = this
        closure() 
    }
}