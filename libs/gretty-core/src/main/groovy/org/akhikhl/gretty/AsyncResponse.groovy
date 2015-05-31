/*
 * Gretty
 *
 * Copyright (C) 2013-2015 Andrey Hihlovskiy and contributors.
 *
 * See the file "LICENSE" for copying and usage permission.
 * See the file "CONTRIBUTORS" for complete list of contributors.
 */
package org.akhikhl.gretty

import org.slf4j.Logger
import org.slf4j.LoggerFactory

import java.util.concurrent.Callable
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.Future

/**
 *
 * @author akhikhl
 */
class AsyncResponse {

  protected static final Logger log = LoggerFactory.getLogger(AsyncResponse)

  final ExecutorService executorService
  final int statusPort
  private listeningForResponseLock = new Object()
  private boolean listeningForResponse

  AsyncResponse(ExecutorService executorService, int statusPort) {
    this.executorService = executorService
    this.statusPort = statusPort
  }

  String getStatus(int servicePort) {
    log.debug 'getStatus({})', servicePort

    Future futureStatus = getResponse()

    def handleConnectionError = { e ->
      log.debug 'Sending "notStarted" to status port...'
      ServiceProtocol.send(statusPort, 'notStarted')
    }

    try {
      log.debug 'Sending "status" command to (probably) running server...'
      ServiceProtocol.send(servicePort, 'status')
    } catch(java.net.ConnectException e) {
      handleConnectionError(e)
    } catch(java.net.SocketException e) {
      handleConnectionError(e)
    }

    futureStatus.get()
  }

  Future getResponse() {
    listeningForResponse = false
    def future = executorService.submit({
      synchronized(listeningForResponseLock) {
        listeningForResponse = true
      }
      ServiceProtocol.readMessage(statusPort)
    } as Callable)
    log.debug 'waiting for AsyncResponse.getResponse to be listen...'
    while(true) {
      synchronized(listeningForResponseLock) {
        if(listeningForResponse)
          break
      }
      Thread.sleep(100)
    }
    log.debug 'AsyncResponse.getResponse is listening.'
    future
  }
}
