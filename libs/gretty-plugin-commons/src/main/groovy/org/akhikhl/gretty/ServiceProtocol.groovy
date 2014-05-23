/*
 * gretty
 *
 * Copyright 2013  Andrey Hihlovskiy.
 *
 * See the file "license.txt" for copying and usage permission.
 */
package org.akhikhl.gretty

final class ServiceProtocol {

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
}

