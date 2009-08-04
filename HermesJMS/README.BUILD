
Building Hermes requires a 1.5 JDK.

You must seprartely obtain the JIDE libraries from http://www.jidesoft.com. If you do any work on Hermes that uses the JIDE API or redistribute Hermes you must contact JIDE for an appropriate license. All of Hermes is Apache 2.0 licensed but it depends on JIDE which is commercial. 

Edit the build.properties to reflect the location of all of your JMS providers. Don't worry if you don't have one installed, if the build script finds one is missing it will skip it and your build won't have the plugin for that provider available. You *do* need a copy of ActiveMQ installed as Hermes uses it to implement message selectors on a JDBC message store.

Take particular care to note how to deal with the JIDE install, you will need to un-jar the libraries as described in the properties file.
In the top directory of the module, invoke ant. Everything should now build. A distribution is automatically created in the directory hermes-X.X containing scripts and libraries. X.X is defined in the build.xml.

Colin Crist, September 2005