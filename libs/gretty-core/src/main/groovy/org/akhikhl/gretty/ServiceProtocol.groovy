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

final class ServiceProtocol {

  protected static final Logger log = LoggerFactory.getLogger(ServiceProtocol)

  static String readMessage(int port) {
    def data
    ServerSocket serverSocket = new ServerSocket(port, 1, InetAddress.getByName('127.0.0.1'))
    try {
      data = readMessageFromServerSocket(serverSocket)
    } finally {
      serverSocket.close()
    }
    return data
  }

  static String readMessageFromServerSocket(ServerSocket serverSocket) {
    def data = new StringBuilder()
    Socket acceptSocket = serverSocket.accept()
    try {
      BufferedReader reader = new BufferedReader(new InputStreamReader(acceptSocket.getInputStream()))
      while(true) {
        String line = reader.readLine()
        if(line == '<<EOF>>')
          break
        data << line
      }
    } finally {
      acceptSocket.close()
    }
    log.debug 'ServiceProtocol.readMessageFromServerSocket({}) -> {}', serverSocket.getLocalPort(), data
    return data
  }

  static void send(int port, String command) {
    log.debug 'ServiceProtocol.send({}, {})', port, command
    Socket s = new Socket(InetAddress.getByName('127.0.0.1'), port)
    try {
      OutputStream out = s.getOutputStream()
      out.write(("${command}\n<<EOF>>\n").getBytes())
      out.flush()
    } finally {
      s.close()
    }
  }
}

