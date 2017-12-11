# HBO-COMMON-GRADLE_BASE

A plugin that applies a set of common plugins for a gradle build. The idea
being you apply this plugin and it will automatically apply a predefined set of
other plugings (like Jacoco, FindBugs, etc...).

Configuration is done via an extension.

Plugins Automatically Applied:

 - groovy
 - java
 - idea
 - spring boot
 - findbugs
 - jacoco
 - buildinfo (for non-pr builds as identified by a -PprBuild=true)

Custom Configurations:

 - Java
 	source and target compatibility set to 1.8

 - FindBugs
 	output's HTML report

 - Jacoco 
 	uses latest Jacoco unless doing Sonar Build (as identified by -Psonarbuild=true)
 	adds check for minimum coverage can be customized by configuring:
		common5 {
 		   minCoverage = 0.0
		}
 	outputs xml and html reports

In addition, provides a shortcut for defining and managing dependencies. You can specify a dependency like:

dependencies {
 compile com5dep("hbo-common-starter-rest")
}

which will resolve to com.hbo.common:hbo-common-starter-rest:5.3.1

The version's used are defined in the plugin. See the file src/main/resources/dependencies.properties for a list of the available mappings and their versions.