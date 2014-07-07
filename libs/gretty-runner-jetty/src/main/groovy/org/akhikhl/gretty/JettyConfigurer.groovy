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

  void applyJettyEnvXml(webAppContext, String jettyEnvXml)

  void applyJettyXml(server, String jettyXml)

  void configureConnectors(server, Map params)

  void configureRealm(context, String realm, String realmConfigFile)

  def createServer()

  def createWebAppContext(List<String> webappClassPath)
  
  List getConfigurations(List<String> webappClassPath)

  void setConfigurationsToWebAppContext(webAppContext, List configurations)

  void setHandlersToServer(server, List handlers)
  
  void setLogger(Logger logger)
}
