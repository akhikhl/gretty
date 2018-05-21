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
import org.gradle.process.JavaForkOptions
import org.slf4j.Logger
import org.slf4j.LoggerFactory
/**
 *
 * @author akhikhl
 */
@CompileStatic(TypeCheckingMode.SKIP)
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
    project.tasks.all { t ->
      if(t.name == thisTask.integrationTestTask) {
        t.dependsOn thisTask
        thisTask.dependsOn { project.tasks.prepareInplaceWebApp }
        thisTask.dependsOn { project.tasks.testClasses }
        if(t.name != 'test' && project.tasks.findByName('test'))
          thisTask.mustRunAfter { project.tasks.test }
        if(GradleUtils.instanceOf(t, 'org.gradle.process.JavaForkOptions')) {
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

    def host = serverStartInfo.host

    task.systemProperty 'gretty.host', host

    def contextPath
    if(task.hasProperty('contextPath') && task.contextPath != null) {
      contextPath = task.contextPath
      if(!contextPath.startsWith('/'))
        contextPath = '/' + contextPath
    }
    else if(serverStartInfo.contexts)
      contextPath = serverStartInfo.contexts[0].contextPath

    task.systemProperty 'gretty.contextPath', contextPath

    String preferredProtocol
    String preferredBaseURI

    def httpPort = serverStartInfo.httpPort
    String httpBaseURI
    if(httpPort) {
      task.systemProperty 'gretty.port', httpPort
      task.systemProperty 'gretty.httpPort', httpPort
      httpBaseURI = "http://${host}:${httpPort}${contextPath}"
      task.systemProperty 'gretty.baseURI', httpBaseURI
      task.systemProperty 'gretty.httpBaseURI', httpBaseURI
      preferredProtocol = 'http'
      preferredBaseURI = httpBaseURI
    }

    def httpsPort = serverStartInfo.httpsPort
    String httpsBaseURI
    if(httpsPort) {
      task.systemProperty 'gretty.httpsPort', httpsPort
      httpsBaseURI = "https://${host}:${httpsPort}${contextPath}"
      task.systemProperty 'gretty.httpsBaseURI', httpsBaseURI
      preferredProtocol = 'https'
      preferredBaseURI = httpsBaseURI
    }

    if(preferredProtocol)
      task.systemProperty 'gretty.preferredProtocol', preferredProtocol
    if(preferredBaseURI)
      task.systemProperty 'gretty.preferredBaseURI', preferredBaseURI
  }
}
