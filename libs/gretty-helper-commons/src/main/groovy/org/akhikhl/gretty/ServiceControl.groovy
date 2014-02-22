/*
 * gretty
 *
 * Copyright 2013  Andrey Hihlovskiy.
 *
 * See the file "license.txt" for copying and usage permission.
 */
package org.akhikhl.gretty

final class ServiceControl {

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

