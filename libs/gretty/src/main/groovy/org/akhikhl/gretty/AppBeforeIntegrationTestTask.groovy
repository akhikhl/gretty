/*
 * gretty
 *
 * Copyright 2013  Andrey Hihlovskiy.
 *
 * See the file "license.txt" for copying and usage permission.
 */
package org.akhikhl.gretty

import org.gradle.process.JavaForkOptions
import org.slf4j.Logger
import org.slf4j.LoggerFactory

/**
 *
 * @author akhikhl
 */
class AppBeforeIntegrationTestTask extends AppStartTask {

  private static final Logger log = LoggerFactory.getLogger(AppBeforeIntegrationTestTask)

  private String integrationTestTask_
  private boolean integrationTestTaskAssigned
  
  AppBeforeIntegrationTestTask() {
    scanInterval = 0 // disable scanning on integration tests
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
    integrationTestTask_ ?: project.gretty.integrationTestTask
  }

  boolean getIntegrationTestTaskAssigned() {
    integrationTestTaskAssigned
  }
  
  @Override
  protected boolean getManagedClassReload() {
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
    project.tasks.all { t ->
      if(t.name == thisTask.integrationTestTask) {
        t.dependsOn thisTask
        thisTask.dependsOn project.tasks.testClasses
        if(t.name != 'test' && project.tasks.findByName('test'))
          thisTask.mustRunAfter project.tasks.test
        if(t instanceof JavaForkOptions) {
          t.doFirst {
            if(thisTask.didWork)
              passSystemPropertiesToIntegrationTask(t)
          }
        }
      }
    }
    integrationTestTaskAssigned = true
  }

  protected void passSystemPropertiesToIntegrationTask(JavaForkOptions task) {

    def launcherConfig = getLauncherConfig()

    def host = launcherConfig.serverConfig.host

    def contextPath = launcherConfig.webAppConfigs[0].contextPath
    task.systemProperty 'gretty.contextPath', contextPath

    def httpPort = launcherConfig.serverConfig.httpPort

    if(httpPort && launcherConfig.serverConfig.httpEnabled) {
      task.systemProperty 'gretty.port', httpPort
      task.systemProperty 'gretty.httpPort', httpPort
      def baseURI = "http://${host}:${httpPort}${contextPath}"
      task.systemProperty 'gretty.baseURI', baseURI
      task.systemProperty 'gretty.httpBaseURI', baseURI
    }

    def httpsPort = launcherConfig.serverConfig.httpsPort

    if(httpsPort && launcherConfig.serverConfig.httpsEnabled) {
      task.systemProperty 'gretty.httpsPort', httpsPort
      task.systemProperty 'gretty.httpsBaseURI', "https://${host}:${httpsPort}${contextPath}"
    }
  }
}
