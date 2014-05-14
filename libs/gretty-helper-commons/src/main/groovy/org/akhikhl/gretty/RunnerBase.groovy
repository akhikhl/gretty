/*
 * gretty
 *
 * Copyright 2013  Andrey Hihlovskiy.
 *
 * See the file "license.txt" for copying and usage permission.
 */
package org.akhikhl.gretty

abstract class RunnerBase {

  protected final Map params
  protected server

  RunnerBase(Map params) {
    this.params = params
  }

  protected void addConfigurationClasses(webAppContext, List<String> webappClassPath) {
  }

  protected abstract void applyJettyEnvXml(webAppContext, jettyEnvXml)

  protected abstract void applyJettyXml()

  protected abstract void configureConnectors()

  protected abstract void configureRealm(context, realmInfo)

  protected abstract createServer()

  protected abstract createWebAppContext(List<String> webappClassPath)

  protected abstract int getServerPort()

  final void run() {
    RunnerThread runnerThread = new RunnerThread(this)
    runnerThread.start()
    runnerThread.join()
  }

  protected abstract void setHandlersToServer(List handlers)

  final void startServer() {
    assert server == null

    if(params.logging)
      LoggingUtils.configureLogging(params.logging)
    else if(params.logbackConfig)
      LoggingUtils.useConfig(params.logbackConfig)

    server = createServer()
    applyJettyXml()
    configureConnectors()

    List handlers = []

    for(def webapp in params.webapps) {
      def context = createWebAppContext(webapp.webappClassPath)
      addConfigurationClasses(context, webapp.webappClassPath)
      applyJettyEnvXml(context, webapp.jettyEnvXml)
      configureRealm(context, webapp.realmInfo)

      context.setContextPath(webapp.contextPath)

      webapp.initParams?.each { key, value ->
        context.setInitParameter(key, value)
      }

      if(webapp.resourceBase != null) {
        if(webapp.inplace)
          context.setResourceBase(webapp.resourceBase)
        else
          context.setWar(webapp.resourceBase)
      }

      handlers.add(context)
    }

    setHandlersToServer(handlers)

    server.start()
  }

  final void stopServer() {
    if(server != null) {
      server.stop()
      server = null
    }
  }
}
