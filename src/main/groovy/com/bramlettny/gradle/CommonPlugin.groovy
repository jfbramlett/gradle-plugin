package com.bramlettny.gradle

import com.bramlettny.buildinfo.gradle.BuildInfoPlugin
import io.franzbecker.gradle.lombok.LombokPlugin
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
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.boot.gradle.plugin.SpringBootPlugin


/**
 * Our gradle plugin used to generate our buildinfo info
 */
class CommonPlugin implements Plugin<Project> {
    private static final Logger logger = LoggerFactory.getLogger(CommonPlugin)

    private CommonDependencyResolver dependencyResolver = new CommonDependencyResolver()

    public void apply(final Project project) {

        project.extensions.create("common", CommonPluginExtension)

        project.plugins.apply(GroovyPlugin)
        project.plugins.apply(JavaPlugin)
        project.plugins.apply(IdeaPlugin)
        project.plugins.apply(SpringBootPlugin)
        project.plugins.apply(FindBugsPlugin)
        project.plugins.apply(JacocoPlugin)
        project.plugins.apply(BuildInfoPlugin)
        project.plugins.apply(LombokPlugin)

        applyJavaConfiguration(project)
        applyFindBugsConfiguration(project)
        applyJacocoConfiguration(project)
        applySpockConfiguration(project)

        resolveDependencies(project)

        if (project.hasProperty('releaseVersion') && !project.releaseVersion.startsWith('${')) {
            project.version = project.releaseVersion
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
        project.dependencies.add("compile", dependencyResolver.com5dep("findbugs"))
    }

    private void applyJacocoConfiguration(final Project project) {
        project.afterEvaluate {
            String jacocoVersion = "0.7.6.201602180812"
            logger.info("Using Jacoco version: " + jacocoVersion)
            project.getExtensions().getByType(JacocoPluginExtension.class).toolVersion = jacocoVersion

            project.tasks.withType(JacocoCoverageVerification) {
                task ->
                    logger.info("Coverage set to " + project.common5.minCoverage)

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

    private void resolveDependencies(final Project project) {
        project.getExtensions().getByType(ExtraPropertiesExtension.class).set("comdep",
                new MethodClosure(dependencyResolver, "comdep"));
    }

    private void applySpockConfiguration(final Project project) {
        if (project.common5.spockTesting) {
            logger.info("Adding spock configuration")
            project.dependencies.add("testCompile", dependencyResolver.com5dep("spock-core"))
            project.dependencies.add("testCompile", dependencyResolver.com5dep("spock-spring"))
            project.dependencies.add("testCompile", dependencyResolver.com5dep("cglib-nodep"))
            project.dependencies.add("testCompile", dependencyResolver.com5dep("spring-boot-starter-test"))
        } else {
            logger.info("Skipping spock configuration")
        }

    }
}