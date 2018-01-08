package com.bramlettny.gradle

import org.apache.commons.text.StrSubstitutor
import org.slf4j.Logger
import org.slf4j.LoggerFactory

class CommonDependencyResolver {
    private final Logger logger = LoggerFactory.getLogger(CommonDependencyResolver)

    final Properties dependencyMap

    public CommonDependencyResolver() {
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
