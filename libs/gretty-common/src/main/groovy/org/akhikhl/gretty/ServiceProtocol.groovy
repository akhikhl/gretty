/*
 * Gretty
 *
 * Copyright (C) 2013-2015 Andrey Hihlovskiy and contributors.
 *
 * See the file "LICENSE" for copying and usage permission.
 * See the file "CONTRIBUTORS" for complete list of contributors.
 */
package org.akhikhl.gretty

import groovy.util.logging.Slf4j

import java.nio.charset.StandardCharsets
import java.util.concurrent.CompletableFuture

@Slf4j
final class ServiceProtocol {

  static Reader createReader(int port  = 0) {
    final ServerSocket serverSocket = new ServerSocket(port, 1, InetAddress.localHost)
    return new Reader(serverSocket)
  }

  static class Reader implements Closeable {
    private final ServerSocket serverSocket

    private Reader(final ServerSocket serverSocket) {
      this.serverSocket = serverSocket
    }

    CompletableFuture<String> readMessageAsync() {
      return CompletableFuture.supplyAsync(this.&readMessage)
    }

    synchronized String readMessage() {
      def data = new StringBuilder()
      Socket acceptSocket = serverSocket.accept()
      try {
        BufferedReader reader = new BufferedReader(new InputStreamReader(acceptSocket.getInputStream(), StandardCharsets.UTF_8))
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

    int getPort() {
      return serverSocket.localPort
    }

    @Override
    void close() {
      serverSocket.close()
    }
  }

  static Writer createWriter(int port) {
    return new Writer(port)
  }

  static class Writer {
    private final int port

    private Writer(final int port) {
      this.port = port
    }

    def write(final String command) {
      log.debug 'ServiceProtocol.send({}, {})', port, command
      Socket s = new Socket(InetAddress.localHost, port)
      try {
        OutputStream out = s.getOutputStream()
        out.write(("${command}\n<<EOF>>\n").getBytes(StandardCharsets.UTF_8))
        out.flush()
      } finally {
        s.close()
      }
    }

    def writeMayFail(final String command) {
      try {
        write(command)
      } catch (e) {
        log.debug(e.getMessage(), e)
      }
    }

    int getPort() {
      return port
    }
  }
}
