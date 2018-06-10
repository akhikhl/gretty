/*
 * Gretty
 *
 * Copyright (C) 2013-2015 Andrey Hihlovskiy and contributors.
 *
 * See the file "LICENSE" for copying and usage permission.
 * See the file "CONTRIBUTORS" for complete list of contributors.
 */
package org.akhikhl.gretty

import groovy.transform.CompileStatic
import groovy.transform.TypeCheckingMode
import org.gradle.api.Task
import org.gradle.process.JavaForkOptions
import org.slf4j.Logger
import org.slf4j.LoggerFactory
/**
 *
 * @author akhikhl
 */
@CompileStatic(TypeCheckingMode.SKIP)
class FarmBeforeIntegrationTestTask extends FarmStartTask {

  protected static final Logger log = LoggerFactory.getLogger(FarmBeforeIntegrationTestTask)

  private String integrationTestTask_
  private boolean integrationTestTaskAssigned

  FarmBeforeIntegrationTestTask() {
    def thisTask = this
    mustRunAfter {
      List projects = project.rootProject.allprojects as List
      def result = []
      int thisProjectIndex = projects.indexOf(project)
      if(thisProjectIndex > 0)
        result.addAll projects[0..thisProjectIndex - 1].findAll { it.extensions.findByName('farms') }.collectMany { proj ->
          proj.extensions.farms.farmsMap.keySet().collect { proj.tasks['farmAfterIntegrationTest' + it] }
        }
      def farms = project.extensions.farms.farmsMap.keySet() as List
      def thisFarmIndex = farms.indexOf(farmName)
      if(thisFarmIndex > 0)
        result.addAll farms[0..thisFarmIndex - 1].collect { project.tasks['farmAfterIntegrationTest' + it] }
      result = result.findAll { otherTask ->
        thisTask.integrationTestTask != otherTask.integrationTestTask || !thisTask.getIntegrationTestProjects().intersect(otherTask.getIntegrationTestProjects())
      }
      result
    }
    doFirst {
      getIntegrationTestProjects().each { proj ->
        proj.tasks.each { t ->
          if(GradleUtils.instanceOf(t, 'org.akhikhl.gretty.AppBeforeIntegrationTestTask') ||
             GradleUtils.instanceOf(t, 'org.akhikhl.gretty.AppAfterIntegrationTestTask'))
            if(t.enabled)
              t.enabled = false
        }
      }
      project.tasks.each { t ->
        if(GradleUtils.instanceOf(t, 'org.akhikhl.gretty.AppBeforeIntegrationTestTask') ||
            GradleUtils.instanceOf(t, 'org.akhikhl.gretty.AppAfterIntegrationTestTask'))
          if(t.enabled)
            t.enabled = false
      }
      project.subprojects.each { proj ->
        proj.tasks.each { t ->
          if(GradleUtils.instanceOf(t, 'org.akhikhl.gretty.AppBeforeIntegrationTestTask') ||
             GradleUtils.instanceOf(t, 'org.akhikhl.gretty.AppAfterIntegrationTestTask') ||
             GradleUtils.instanceOf(t, 'org.akhikhl.gretty.FarmBeforeIntegrationTestTask') ||
             GradleUtils.instanceOf(t, 'org.akhikhl.gretty.FarmAfterIntegrationTestTask') ||
             GradleUtils.instanceOf(t, 'org.akhikhl.gretty.FarmIntegrationTestTask'))
            if(t.enabled)
              t.enabled = false
        }
      }
    }
  }

  @Override
  protected boolean getDefaultJacocoEnabled() {
    true
  }

  @Override
  protected boolean getIntegrationTest() {
    true
  }

  String getIntegrationTestTask() {
    integrationTestTask_ ?: new FarmConfigurer(project).getProjectFarm(farmName).integrationTestTask
  }

  boolean getIntegrationTestTaskAssigned() {
    integrationTestTaskAssigned
  }

  @Override
  protected boolean getManagedClassReload(ServerConfig sconfig) {
    // disable managed class reloads on integration tests
    false
  }

  void integrationTestTask(String integrationTestTask) {
    if(integrationTestTaskAssigned) {
      log.warn '{}.integrationTestTask is already set to "{}", so "{}" is ignored', name, getIntegrationTestTask(), integrationTestTask
      return
    }
    integrationTestTask_ = integrationTestTask
    def thisTask = this
    project.rootProject.allprojects.each { proj ->
      proj.tasks.all { Task t ->
        if(getIntegrationTestProjects().contains(proj)) {
          if (t.name == thisTask.integrationTestTask) {
            t.mustRunAfter thisTask
            thisTask.mustRunAfter proj.tasks.testClasses
            if (t.name != 'test' && project.tasks.findByName('test'))
              thisTask.mustRunAfter project.tasks.test
            if (GradleUtils.instanceOf(t, 'org.gradle.process.JavaForkOptions'))
              t.doFirst {
                if (thisTask.didWork)
                  passSystemPropertiesToIntegrationTestTask(t, t)
              }
          } else if (GradleUtils.instanceOf(t, 'org.akhikhl.gretty.AppBeforeIntegrationTestTask') && t.integrationTestTask == thisTask.integrationTestTask)
            t.mustRunAfter thisTask // need this to be able to disable AppBeforeIntegrationTestTask in doFirst
        }
      }
    }
    integrationTestTaskAssigned = true
  }

  protected void passSystemPropertiesToIntegrationTestTask(Task integrationTestTask, JavaForkOptions javaForkOptions) {

    def host = serverStartInfo.host

    javaForkOptions.systemProperty 'gretty.host', host

    String contextPath
    if(integrationTestTask.ext.has('contextPath') && integrationTestTask.ext.contextPath != null) {
      contextPath = integrationTestTask.ext.contextPath
      if(!contextPath.startsWith('/'))
        contextPath = '/' + contextPath
    }
    else {
      Iterable<WebAppConfig> webAppConfigs = getStartConfig().getWebAppConfigs()
      if(webAppConfigs) {
        WebAppConfig webAppConfig = webAppConfigs.find { it.projectPath == integrationTestTask.project.path }
        if (webAppConfig == null)
          webAppConfig = webAppConfigs.first()
        contextPath = webAppConfig.contextPath
      }
    }

    javaForkOptions.systemProperty 'gretty.contextPath', contextPath

    String preferredProtocol
    String preferredBaseURI

    def httpPort = serverStartInfo.httpPort
    String httpBaseURI
    if(httpPort) {
      javaForkOptions.systemProperty 'gretty.port', httpPort
      javaForkOptions.systemProperty 'gretty.httpPort', httpPort
      httpBaseURI = "http://${host}:${httpPort}${contextPath}"
      javaForkOptions.systemProperty 'gretty.baseURI', httpBaseURI
      javaForkOptions.systemProperty 'gretty.httpBaseURI', httpBaseURI
      preferredProtocol = 'http'
      preferredBaseURI = httpBaseURI
    }

    def httpsPort = serverStartInfo.httpsPort
    String httpsBaseURI
    if(httpsPort) {
      javaForkOptions.systemProperty 'gretty.httpsPort', httpsPort
      httpsBaseURI = "https://${host}:${httpsPort}${contextPath}"
      javaForkOptions.systemProperty 'gretty.httpsBaseURI', httpsBaseURI
      preferredProtocol = 'https'
      preferredBaseURI = httpsBaseURI
    }

    if(preferredProtocol)
      javaForkOptions.systemProperty 'gretty.preferredProtocol', preferredProtocol
    if(preferredBaseURI)
      javaForkOptions.systemProperty 'gretty.preferredBaseURI', preferredBaseURI

    javaForkOptions.systemProperty 'gretty.farm', (farmName ?: 'default')
  }
}
