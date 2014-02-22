/*
 * gretty
 *
 * Copyright 2013  Andrey Hihlovskiy.
 *
 * See the file "license.txt" for copying and usage permission.
 */
package org.akhikhl.gretty

final class ServiceControl {

  static String readMessage(int port) {
    ServerSocket socket = new ServerSocket(port, 1, InetAddress.getByName('127.0.0.1'))
    try {
      Socket acceptSocket = socket.accept()
      try {
        BufferedReader reader = new BufferedReader(new InputStreamReader(acceptSocket.getInputStream()))
        return reader.readLine()
      } finally {
        acceptSocket.close()
      }
    } finally {
      socket.close()
    }
  }

  static void send(int port, String command) {
    Socket s = new Socket(InetAddress.getByName('127.0.0.1'), port)
    try {
      OutputStream out = s.getOutputStream()
      out.write(("${command}\n").getBytes())
      out.flush()
    } finally {
      s.close()
    }
  }
}

