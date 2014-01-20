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
  protected contextPath

  RunnerBase(Map params) {
    this.params = params
  }

  protected void addConfigurationClasses(webAppContext) {
  }

  protected void addConfigurationClassesToServer() {
  }

  protected void applyContainerIncludeJarPattern(webAppContext) {
  }

  protected abstract void applyJettyEnvXml(webAppContext)

  protected abstract void applyJettyXml()

  protected abstract void configureConnectors()

  protected abstract void configureRealm(context)

  protected abstract createServer()

  protected abstract createWebAppContext(ClassLoader classLoader)

  protected abstract int getServerPort()

  final void run() {

    startServer()

    Thread monitor = new MonitorThread(this)
    monitor.start()

    System.out.println 'Jetty server started.'
    System.out.println 'You can see web-application in browser under the address:'
    System.out.println "http://localhost:${getServerPort()}${contextPath}"

    if(params.interactive)
      System.out.println 'Press any key to stop the jetty server.'
    else
      System.out.println 'Enter \'gradle jettyStop\' to stop the jetty server.'
    System.out.println()

    if(params.interactive) {
      System.in.read()
      if(monitor.running)
        ServiceControl.send(params.servicePort, 'stop')
    }

    monitor.join()

    System.out.println 'Jetty server stopped.'
  }

  final void startServer() {
    assert server == null

    Set<URL> classpathUrls = params.projectClassPath.collect { new URL(it) } as LinkedHashSet

    if(params.logging)
      LoggingUtils.configureLogging(params.logging)
    else if(params.logbackConfig)
      LoggingUtils.useConfig(params.logbackConfig)

    server = createServer()
    applyJettyXml()
    addConfigurationClassesToServer()
    configureConnectors()

    ClassLoader classLoader = new URLClassLoader(classpathUrls as URL[], this.getClass().getClassLoader())
    def context = createWebAppContext(classLoader)
    addConfigurationClasses(context)
    applyJettyEnvXml(context)
    applyContainerIncludeJarPattern(context)
    configureRealm(context)

    if(params.contextPath == null) {
      if(context.getContextPath() == '/')
        context.setContextPath('/' + params.projectName)
    } else if(params.contextPath.startsWith('/'))
      context.setContextPath(params.contextPath)
    else
      context.setContextPath('/' + params.contextPath)

    contextPath = context.getContextPath()

    params.initParams?.each { key, value ->
      context.setInitParameter(key, value)
    }

    if(params.resourceBase != null) {
      if(params.inplace)
        context.setResourceBase(params.resourceBase)
      else
        context.setWar(params.resourceBase)
    }

    context.setServer(server)
    server.setHandler(context)

    server.start()
  }

  final void stopServer() {
    server.stop()
    server = null
  }
}
