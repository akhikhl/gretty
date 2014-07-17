/*
 * gretty
 *
 * Copyright 2013  Andrey Hihlovskiy.
 *
 * See the file "license.txt" for copying and usage permission.
 */
package org.akhikhl.gretty

/**
 *
 * @author akhikhl
 */
interface ServerManager {
	void setParams(Map params)
  void startServer()
  void startServer(ServerStartEvent startEvent)
  void stopServer()
}

