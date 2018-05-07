package org.akhikhl.gretty.internal.integrationTests

import org.akhikhl.gretty.ServletContainerConfig
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.publish.maven.MavenPublication
import org.slf4j.Logger
import org.slf4j.LoggerFactory

class BasePlugin implements Plugin<Project> {

  private static final Logger log = LoggerFactory.getLogger(BasePlugin)

  protected void applyPlugins(Project project) {
    if(!project.plugins.findPlugin('maven-publish'))
      project.apply plugin: 'maven-publish'
  }

  protected void applyPluginsToRootProject(Project project) {
    // does nothing by default
  }

  protected void configureDependencies(Project project) {
    // does nothing by default
  }

  protected void configureExtensions(Project project) {
    // does nothing by default
  }

  protected void configurePublications(Project project) {

    if(project.tasks.findByName('publishToPrivateRepository'))
      return

    project.publishing {
      publications {
        if(project.plugins.findPlugin('war'))
          mavenWeb(MavenPublication) {
            from project.components.web
          }
        if(project.plugins.findPlugin('java'))
          mavenJava(MavenPublication) {
            from project.components.java
          }
      }
      repositories {
        maven {
          name 'private'
          url "file:/${project.privateRepoDir}"
        }
      }
    }

    project.task('publishToPrivateRepository') {
      if(project.plugins.findPlugin('war'))
        dependsOn "publishMavenWebPublicationToPrivateRepository"
      if(project.plugins.findPlugin('java'))
        dependsOn "publishMavenJavaPublicationToPrivateRepository"
      if(project.tasks.findByName('check'))
        mustRunAfter project.tasks.check
    }
  }

  protected void configureRepositories(Project project) {
    if(project.repositories.find { it.name == 'privateRepo' })
      return
    project.repositories {
      assert project.hasProperty('privateRepoDir')
      maven {
        name 'privateRepo'
        url "file:${project.privateRepoDir}"
      }
      jcenter()
    }
  }

  protected void configureRootProjectProperties(Project project) {
    for(prop in ['gebVersion', 'geckoDriverVersion', 'geckoDriverPlatform', 'groovy_version', 'seleniumVersion', 'spock_version', 'testAllContainers']) {
      if(!project.hasProperty(prop)) {
        project.ext[prop] = ProjectProperties.getString(prop)
      }
    }
  }

  protected void configureRootProjectTasksAfterEvaluate(Project project) {
    // does nothing by default
  }

  protected void configureSourceSets(Project project) {
    // does nothing by default
  }

  protected void configureTasks(Project project) {

    if(!project.rootProject.tasks.findByName('testAll'))
      project.rootProject.task 'testAll'

    if(!project.rootProject.tasks.findByName('wrapper'))
      project.rootProject.task('wrapper', type: Wrapper) {
        gradleVersion = '2.14.1'
      }
  }

  protected void configureTasksAfterEvaluate(Project project) {
    // does nothing by default
  }

  void apply(Project project) {
    log.info 'Applying {}:{}:{} to {}',
      ProjectProperties.getString('projectGroup'),
      ProjectProperties.getString('projectName'),
      ProjectProperties.getString('projectVersion'),
      project

    applyPlugins(project)
    applyPluginsToRootProject(project.rootProject)
    configureRootProjectProperties(project.rootProject)
    configureRepositories(project)
    configureExtensions(project)
    configureSourceSets(project)
    configureTasks(project)

    project.afterEvaluate {

      configurePublications(project)
      configureRootProjectTasksAfterEvaluate(project.rootProject)
      configureDependencies(project)
      configureTasksAfterEvaluate(project)

    } // afterEvaluate
  }
}
