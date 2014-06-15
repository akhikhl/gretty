/*
 * gretty
 *
 * Copyright 2013  Andrey Hihlovskiy.
 *
 * See the file "license.txt" for copying and usage permission.
 */
package org.akhikhl.gretty

import groovy.json.JsonSlurper

abstract class RunnerBase {

  protected final Map params
  protected boolean paramsLoaded = false

  RunnerBase(Map params) {
    this.params = params
  }
  
  protected abstract ServerManager createServerManager()

  final void run() {
    ServerManager serverManager = createServerManager()
    try {
      ServerSocket socket = new ServerSocket(params.servicePort, 1, InetAddress.getByName('127.0.0.1'))
      try {
        ServiceProtocol.send(params.statusPort, 'init')
        while(true) {
          def data = ServiceProtocol.readMessageFromServerSocket(socket)
          if(!paramsLoaded) {
            params << new JsonSlurper().parseText(data)
            paramsLoaded = true
            serverManager.setParams(params)
            serverManager.startServer()
            ServiceProtocol.send(params.statusPort, 'started')
            // Note that server is already in listening state.
            // If client sends a command immediately after 'started' signal,
            // the command is queued, so that socket.accept gets it anyway.
            continue
          }
          if(data == 'stop') {
            serverManager.stopServer()
            break
          }
          else if(data == 'restart') {
            serverManager.stopServer()
            serverManager.startServer()
          }
        }
      } finally {
        socket.close()
      }
    } catch(Exception e) {
      throw new RuntimeException(e)
    }
  }
}
