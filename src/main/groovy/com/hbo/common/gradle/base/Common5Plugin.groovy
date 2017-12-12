package com.hbo.common.gradle.base

import com.hbo.common.buildinfo.gradle.BuildInfoPlugin
import org.codehaus.groovy.runtime.MethodClosure
import org.gradle.api.Action
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.plugins.ExtraPropertiesExtension
import org.gradle.api.plugins.GroovyPlugin
import org.gradle.api.plugins.JavaPlugin
import org.gradle.api.plugins.quality.FindBugs
import org.gradle.api.plugins.quality.FindBugsPlugin
import org.gradle.api.tasks.compile.JavaCompile
import org.gradle.api.tasks.testing.Test
import org.gradle.plugins.ide.idea.IdeaPlugin
import org.gradle.testing.jacoco.plugins.JacocoPlugin
import org.gradle.testing.jacoco.plugins.JacocoPluginExtension
import org.gradle.testing.jacoco.tasks.JacocoCoverageVerification
import org.gradle.testing.jacoco.tasks.JacocoReport
import org.gradle.testing.jacoco.tasks.rules.JacocoLimit
import org.gradle.testing.jacoco.tasks.rules.JacocoViolationRule
import org.sonarqube.gradle.SonarQubePlugin
import org.springframework.boot.gradle.plugin.SpringBootPlugin

/**
 * Our gradle plugin used to generate our buildinfo info
 */
class Common5Plugin implements Plugin<Project> {


    public void apply(final Project project) {

        project.extensions.create("common5", HboCommonPluginExtension)

        // do this first as we configure Sonar via System properties
        applySonarQubeConfiguration(project)

        project.plugins.apply(GroovyPlugin)
        project.plugins.apply(JavaPlugin)
        project.plugins.apply(IdeaPlugin)
        project.plugins.apply(SpringBootPlugin)
        project.plugins.apply(FindBugsPlugin)
        project.plugins.apply(JacocoPlugin)
        project.plugins.apply(SonarQubePlugin)
        if (!(project.hasProperty("prBuild") && project.prBuild.equals("true"))) {
            project.plugins.apply(BuildInfoPlugin)
        }


        applyJavaConfiguration(project)
        applyFindBugsConfiguration(project)
        applyJacocoConfiguration(project)

        resolveDependencies(project)

        //project.extensions.add("common5Version", "5.3.1")
        if (project.hasProperty('releaseVersion') && !project.releaseVersion.startsWith('${')) {
            project.version = project.releaseVersion
        } else {
            project.version = '1.0-SNAPSHOT'
        }
    }

    private void applyJavaConfiguration(final Project project) {
        project.tasks.withType(JavaCompile) {
            task ->
                task.properties.put("options.encoding", "UTF-8")
                task.setSourceCompatibility("1.8")
                task.setTargetCompatibility("1.8")
        }
    }

    private void applyFindBugsConfiguration(final Project project) {
        // configure findbugs
        project.tasks.withType(FindBugs) {
            task ->
                task.reports.xml.enabled = false
                task.reports.html.enabled = true
        }
        project.dependencies.add("compile", "com.google.code.findbugs:findbugs:3.0.1")
    }

    private void applyJacocoConfiguration(final Project project) {
        // configure Jacoco
        String jacocoVersion = "0.7.6.201602180812"
        if (project.hasProperty("sonarbuild")) {
            jacocoVersion = "0.7.3.201502191951"
        }
        project.tasks.withType(JacocoPluginExtension) {
            task ->
                task.toolVersion = jacocoVersion
        }

        project.afterEvaluate {
            project.tasks.withType(JacocoCoverageVerification) {
                task ->
                    println("Coverage set to " + project.common5.minCoverage)

                    task.violationRules.rule(new Action<JacocoViolationRule>() {
                        @Override
                        void execute(JacocoViolationRule jacocoViolationRule) {
                            jacocoViolationRule.limit(new Action<JacocoLimit>() {
                                @Override
                                void execute(JacocoLimit jacocoLimit) {
                                    jacocoLimit.minimum = new BigDecimal(project.common5.minCoverage)
                                }
                            })
                        }
                    })
                    task.violationRules.failOnViolation = true
            }
        }

        project.tasks.withType(JacocoReport) {
            task ->
                task.reports.xml.enabled = true
                task.reports.html.enabled = true
        }


        project.test
        project.tasks.withType(Test) {
            task ->
                Set requiredTasks = new HashSet(project.getTasksByName("jacocoTestReport", false))
                requiredTasks.addAll(project.getTasksByName("jacocoTestCoverageVerification", false))
                task.finalizedBy(requiredTasks)
        }
    }

    private void applySonarQubeConfiguration(final Project project) {
        System.setProperty("sonar.jacoco.reportPath", project.buildDir.getAbsolutePath() + "/jacoco/test/exec")
        System.setProperty("sonar.java.coveragePlugin", "jacoco")
    }

    private void resolveDependencies(final Project project) {
        Common5DependencyResolver resolver = new Common5DependencyResolver(project);
        project.getExtensions().getByType(ExtraPropertiesExtension.class).set("com5dep",
                new MethodClosure(resolver, "com5dep"));
    }
}