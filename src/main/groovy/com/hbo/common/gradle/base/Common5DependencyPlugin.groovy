package com.hbo.common.gradle.base

import org.gradle.api.artifacts.DependencyResolveDetails
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.slf4j.Logger
import org.slf4j.LoggerFactory

class Common5DependencyPlugin implements Plugin<Project> {
    Logger logger = LoggerFactory.getLogger(Common5DependencyPlugin)

    def Map<String, String> dependencyAliasesDef = [:]

    @Override
    void apply(Project project) {
        loadAliases()

        project.getConfigurations().all {config ->
            config.resolutionStrategy.eachDependency { DependencyResolveDetails details ->
                if (details.requested.getVersion() == "default") {
                    def key = details.requested.getGroup() + "." + details.requested.getName()
                    logger.info("Looking up " + key)
                    details.useVersion(dependencyAliasesDef.get(key))
                }
            }
        }
    }

    void loadAliases() {
        def properties = new Properties()
        this.getClass().getResource( '/dependencies.properties' ).withInputStream {
            properties.load(it)
        }
        for (String s : properties.stringPropertyNames()) {
            logger.info("Found prop: " + s + " -" + properties.getProperty(s))
        }
        dependencyAliasesDef.putAll(properties)
    }
}
