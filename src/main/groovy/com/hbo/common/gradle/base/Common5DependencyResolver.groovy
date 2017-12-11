package com.hbo.common.gradle.base

import org.apache.commons.text.StrSubstitutor
import org.gradle.api.Project
import org.slf4j.Logger
import org.slf4j.LoggerFactory

class Common5DependencyResolver {
    private final Logger logger = LoggerFactory.getLogger(Common5DependencyResolver)

    final Project project
    final Properties dependencyMap

    public Common5DependencyResolver(final Project project) {
        this.project = project

        // load our properties
        dependencyMap = new Properties()
        this.getClass().getResource( '/dependencies.properties' ).withInputStream {
            dependencyMap.load(it)
        }
    }

    public String com5dep(String dependencyNotation) throws Exception {
        String dependency = dependencyMap.getProperty(dependencyNotation)
        if (dependency == null) {
            logger.error("Failed to find dependency for " + dependencyNotation)
            dependency = dependencyNotation
        }

        return StrSubstitutor.replace(dependency, dependencyMap)
    }

}
