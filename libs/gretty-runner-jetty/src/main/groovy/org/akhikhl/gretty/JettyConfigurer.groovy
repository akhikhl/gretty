/*
 * gretty
 *
 * Copyright 2013  Andrey Hihlovskiy.
 *
 * See the file "license.txt" for copying and usage permission.
 */
package org.akhikhl.gretty

import org.slf4j.Logger

/**
 *
 * @author akhikhl
 */
interface JettyConfigurer {

  void addLifeCycleListener(server, listener)

  void applyContextConfigFile(webAppContext, URL contextConfigFile)

  void applyJettyXml(server, String jettyXml)

  void configureConnectors(server, Map serverParams)

  void configureSecurity(context, Map serverParams, Map webappParams)

  void configureSessionManager(server, context, Map serverParams, Map webappParams)

  def createServer()

  def createWebAppContext(List<String> webappClassPath)

  def findHttpConnector(server)

  def findHttpsConnector(server)

  List getConfigurations(List<String> webappClassPath)

  void setConfigurationsToWebAppContext(context, List configurations)

  void setHandlersToServer(server, List handlers)

  void setLogger(Logger logger)
}
