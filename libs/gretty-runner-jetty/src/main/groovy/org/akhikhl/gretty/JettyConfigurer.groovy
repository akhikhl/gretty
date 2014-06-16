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

  void addConfigurationClasses(webAppContext, List<String> webappClassPath)

  void applyJettyEnvXml(webAppContext, String jettyEnvXml)

  void applyJettyXml(server, String jettyXml)

  void configureConnectors(server, Map params)

  void configureRealm(context, String realm, String realmConfigFile)

  def createServer()

  def createWebAppContext(List<String> webappClassPath)

  void setHandlersToServer(server, List handlers)
  
  void setLogger(Logger logger)
}

