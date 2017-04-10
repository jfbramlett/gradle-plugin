package com.hbo.common.gradle.base

import com.hbo.common.buildinfo.gradle.BuildInfoPlugin
import com.palantir.jacoco.JacocoCoveragePlugin
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.plugins.GroovyPlugin
import org.gradle.api.plugins.JavaPlugin
import org.gradle.api.plugins.quality.FindBugs
import org.gradle.api.plugins.quality.FindBugsPlugin
import org.gradle.api.tasks.compile.JavaCompile
import org.gradle.plugins.ide.idea.IdeaPlugin
import org.springframework.boot.gradle.plugin.SpringBootPlugin

/**
 * Our gradle plugin used to generate our buildinfo info
 */
class BasePlugin implements Plugin<Project> {


    public void apply(final Project project) {
        project.plugins.apply(GroovyPlugin)
        project.plugins.apply(JavaPlugin)
        project.plugins.apply(IdeaPlugin)
        project.plugins.apply(SpringBootPlugin)
        project.plugins.apply(JacocoCoveragePlugin)
        project.plugins.apply(BuildInfoPlugin)
        project.plugins.apply(FindBugsPlugin)

        project.tasks.withType(JavaCompile) {
            task -> task.properties.put("options.encoding", "UTF-8")
        }

        project.tasks.withType(FindBugs) {
            task ->
                task.reports.xml.enabled = false
                task.reports.html.enabled = true
        }

        project.dependencies.add("compile", "com.google.code.findbugs:findbugs:3.0.1")

    }
}
