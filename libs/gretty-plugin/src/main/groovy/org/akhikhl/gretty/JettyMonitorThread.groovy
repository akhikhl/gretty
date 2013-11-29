/*
 * gretty
 *
 * Copyright 2013  Andrey Hihlovskiy.
 *
 * See the file "license.txt" for copying and usage permission.
 */
package org.akhikhl.gretty

final class JettyMonitorThread extends Thread {

  private final def server
  private ServerSocket socket
  private boolean running = false

  public JettyMonitorThread(int servicePort, def server) {
    this.server = server
    daemon = false
    name = 'JettyMonitorThread'
    try {
      socket = new ServerSocket(servicePort, 1, InetAddress.getByName('127.0.0.1'))
    } catch(Exception e) {
      throw new RuntimeException(e)
    }
  }

  public boolean getRunning() {
    synchronized(this) {
      return running
    }
  }

  @Override
  public void run() {
    try {
      synchronized(this) {
        running = true
      }
      try {
        while(true) {
          String command
          Socket accept = socket.accept()
          try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(accept.getInputStream()))
            command = reader.readLine()
          } finally {
            accept.close()
          }
          if(command == 'stop') {
            server.stop()
            break
          }
          if(command == 'restart') {
            server.stop()
            server.start()
          }
          // more commands could be inserted here
        }
      } finally {
        synchronized(this) {
          running = false
        }
        socket.close()
      }
    } catch(Exception e) {
      throw new RuntimeException(e)
    }
  }
}
