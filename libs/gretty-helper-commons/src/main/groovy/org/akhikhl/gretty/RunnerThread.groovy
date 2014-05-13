/*
 * gretty
 *
 * Copyright 2013  Andrey Hihlovskiy.
 *
 * See the file "license.txt" for copying and usage permission.
 */
package org.akhikhl.gretty

import org.slf4j.Logger
import org.slf4j.LoggerFactory

final class RunnerThread extends Thread {

  private final RunnerBase runner

  RunnerThread(RunnerBase runner) {
    this.runner = runner
    daemon = false
    name = 'RunnerThread'
  }

  @Override
  void run() {
    try {
      ServerSocket socket = new ServerSocket(runner.params.servicePort, 1, InetAddress.getByName('127.0.0.1'))
      try {
        runner.startServer()
        ServiceControl.send(runner.params.statusPort, 'started')
        // Note that server is already in listening state.
        // If client sends a command immediately after 'started' signal,
        // the command is queued, so that socket.accept gets it anyway.
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
          else if(command == 'restart') {
            runner.stopServer()
            runner.startServer()
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
