package com.hbo.common.gradle.base

import com.hbo.common.buildinfo.gradle.BuildInfoPlugin
import org.gradle.api.Action
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.plugins.GroovyPlugin
import org.gradle.api.plugins.JavaPlugin
import org.gradle.api.plugins.quality.FindBugs
import org.gradle.api.plugins.quality.FindBugsPlugin
import org.gradle.api.tasks.compile.JavaCompile
import org.gradle.plugins.ide.idea.IdeaPlugin
import org.gradle.testing.jacoco.plugins.JacocoPlugin
import org.gradle.testing.jacoco.tasks.JacocoCoverageVerification
import org.gradle.testing.jacoco.tasks.rules.JacocoLimit
import org.gradle.testing.jacoco.tasks.rules.JacocoViolationRule
import org.springframework.boot.gradle.plugin.SpringBootPlugin

/**
 * Our gradle plugin used to generate our buildinfo info
 */
class BasePlugin implements Plugin<Project> {


    public void apply(final Project project) {

        project.extensions.create("hboCommon", HboCommonPluginExtension)

        project.plugins.apply(GroovyPlugin)
        project.plugins.apply(JavaPlugin)
        project.plugins.apply(IdeaPlugin)
        project.plugins.apply(SpringBootPlugin)
        project.plugins.apply(JacocoPlugin)
        project.plugins.apply(BuildInfoPlugin)
        project.plugins.apply(FindBugsPlugin)


        project.tasks.withType(JavaCompile) {
            task -> task.properties.put("options.encoding", "UTF-8")
        }

        // configure findbugs
        project.tasks.withType(FindBugs) {
            task ->
                task.reports.xml.enabled = false
                task.reports.html.enabled = true
        }
        project.dependencies.add("compile", "com.google.code.findbugs:findbugs:3.0.1")

        // configure jacoco coverage
        project.afterEvaluate {
            project.tasks.withType(JacocoCoverageVerification) {
                task ->
                    println("Coverage set to " + project.hboCommon.minCoverage)

                    task.violationRules.rule(new Action<JacocoViolationRule>() {
                        @Override
                        void execute(JacocoViolationRule jacocoViolationRule) {
                            jacocoViolationRule.limit(new Action<JacocoLimit>() {
                                @Override
                                void execute(JacocoLimit jacocoLimit) {
                                    jacocoLimit.minimum = new BigDecimal(project.hboCommon.minCoverage)
                                }
                            })
                        }
                    })
                    task.violationRules.failOnViolation = true
            }
        }

        // configure our release version
        if (project.hasProperty('releaseVersion') && !project.releaseVersion.startsWith('${')) {
            project.version = project.releaseVersion
        } else {
            project.version = '1.0-SNAPSHOT'
        }

    }
}