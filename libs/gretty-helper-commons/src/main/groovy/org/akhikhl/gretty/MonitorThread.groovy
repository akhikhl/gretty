/*
 * gretty
 *
 * Copyright 2013  Andrey Hihlovskiy.
 *
 * See the file "license.txt" for copying and usage permission.
 */
package org.akhikhl.gretty

final class MonitorThread extends Thread {

  private final RunnerBase runner
  private ServerSocket socket
  private boolean running = false

  public MonitorThread(RunnerBase runner) {
    this.runner = runner
    daemon = false
    name = 'MonitorThread'
    try {
      socket = new ServerSocket(runner.params.servicePort, 1, InetAddress.getByName('127.0.0.1'))
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
      ServiceControl.send(7777, 'started')
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
            runner.stopServer()
            break
          }
          if(command == 'restart') {
            runner.stopServer()
            runner.startServer()
          }
          // more commands could be inserted here
        }
      } finally {
        socket.close()
        synchronized(this) {
          running = false
        }
        ServiceControl.send(7777, 'stopped')
      }
    } catch(Exception e) {
      throw new RuntimeException(e)
    }
  }
}
