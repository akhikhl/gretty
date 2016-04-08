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

  private static final Logger logger = LoggerFactory.getLogger(ServiceProtocol.class)

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
    return data
  }

  static void send(int port, String command) {
    Socket s = new Socket(InetAddress.getByName('127.0.0.1'), port)
    try {
      OutputStream out = s.getOutputStream()
      out.write(("${command}\n<<EOF>>\n").getBytes())
      out.flush()
    } finally {
      s.close()
    }
  }

  static void sendMayFail(int port, String command) {
    Socket s = null
    try {
      s = new Socket(InetAddress.getByName('127.0.0.1'), port)
      OutputStream out = s.getOutputStream()
      out.write(("${command}\n<<EOF>>\n").getBytes())
      out.flush()
    } catch (ConnectException ignored) {
      logger.debug(ignored.getMessage(), ignored)
    } finally {
      s?.close()
    }
  }
}

