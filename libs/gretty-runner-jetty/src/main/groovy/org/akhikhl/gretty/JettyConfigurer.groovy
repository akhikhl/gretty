/*
 * Gretty
 *
 * Copyright (C) 2013-2014 Andrey Hihlovskiy and contributors.
 *
 * See the file "LICENSE" for copying and usage permission.
 * See the file "CONTRIBUTORS" for complete list of contributors.
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

  void configureSecurity(context, String realm, String realmConfigFile, boolean singleSignOn)

  void configureSessionManager(server, context, Map serverParams, Map webappParams)

  def createResourceCollection(List paths)

  def createServer()

  def createWebAppContext(Map serverParams, Map webappParams)

  def findHttpConnector(server)

  def findHttpsConnector(server)

  URL findResourceURL(baseResource, String path)

  List getConfigurations(Map webappParams)

  void setConfigurationsToWebAppContext(context, List configurations)

  void setHandlersToServer(server, List handlers)
}
