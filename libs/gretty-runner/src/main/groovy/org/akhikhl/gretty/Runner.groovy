/*
 * gretty
 *
 * Copyright 2013  Andrey Hihlovskiy.
 *
 * See the file "license.txt" for copying and usage permission.
 */
package org.akhikhl.gretty

import groovy.json.JsonSlurper

/**
 *
 * @author akhikhl
 */
final class Runner {

  protected final Map params
  protected boolean paramsLoaded = false

  static void main(String[] args) {
    def cli = new CliBuilder()
    cli.with {
      sv longOpt: 'servicePort', required: true, args: 1, argName: 'servicePort', type: Integer, 'service port'
      st longOpt: 'statusPort', required: true, args: 1, argName: 'statusPort', type: Integer, 'status port'
      smf longOpt: 'serverManagerFactory', required: true, args: 1, argName: 'serverManagerFactory', type: String, 'server manager factory'
    }
    def options = cli.parse(args)
    Map params = [ servicePort: options.servicePort as int, statusPort: options.statusPort as int, serverManagerFactory: options.serverManagerFactory ]
    new Runner(params).run()
  }

  private Runner(Map params) {
    this.params = params
  }

  private void run() {
    def ServerManagerFactory = Class.forName(params.serverManagerFactory, true, this.getClass().classLoader)
    ServerManager serverManager = ServerManagerFactory.createServerManager()
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
