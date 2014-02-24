/*
 * gretty
 *
 * Copyright 2013  Andrey Hihlovskiy.
 *
 * See the file "license.txt" for copying and usage permission.
 */
package org.akhikhl.gretty

import java.util.concurrent.locks.Condition
import java.util.concurrent.locks.Lock
import java.util.concurrent.locks.ReentrantLock

final class RunnerThread extends Thread {

  private final RunnerBase runner
  private final Object runningLock = new Object()
  private boolean running = false
  private final Object runningSignal = new Object()
  private final Object stateChangeSignal = new Object()

  RunnerThread(RunnerBase runner) {
    this.runner = runner
    daemon = false
    name = 'RunnerThread'
  }

  boolean getRunning() {
    synchronized(runningLock) {
      return running
    }
  }

  @Override
  void run() {
    try {
      ServerSocket socket
      try {
        socket = new ServerSocket(runner.params.servicePort, 1, InetAddress.getByName('127.0.0.1'))
      } catch(Exception e) {
        throw new RuntimeException(e)
      }
      synchronized(runningLock) {
        running = true
      }
      synchronized(runningSignal) {
        runningSignal.notifyAll()
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
          if(command == 'start') {
            runner.startServer()
            synchronized(stateChangeSignal) {
              stateChangeSignal.notifyAll()
            }
            if(runner.params.integrationTest)
              ServiceControl.send(runner.params.integrationTestStatusPort, 'started')
          }
          else if(command == 'stop') {
            runner.stopServer()
            synchronized(stateChangeSignal) {
              stateChangeSignal.notifyAll()
            }
            if(runner.params.integrationTest)
              ServiceControl.send(runner.params.integrationTestStatusPort, 'stopped')
            break
          }
          else if(command == 'restart') {
            runner.stopServer()
            runner.startServer()
          }
          // more commands could be inserted here
        }
      } finally {
        socket.close()
        synchronized(runningLock) {
          running = false
        }
      }
    } catch(Exception e) {
      throw new RuntimeException(e)
    }
  }

  void waitForRunning() {
    synchronized(runningSignal) {
      runningSignal.wait()
    }
  }

  void waitForStateChange() {
    synchronized(stateChangeSignal) {
      stateChangeSignal.wait()
    }
  }
}
