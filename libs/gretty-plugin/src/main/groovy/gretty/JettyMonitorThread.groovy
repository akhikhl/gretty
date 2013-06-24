package gretty

import org.eclipse.jetty.server.Server

final class JettyMonitorThread extends Thread {

  private final Server server
  private ServerSocket socket

  public JettyMonitorThread(int servicePort, final Server server) {
    this.server = server
    setDaemon false
    setName 'JettyMonitorThread'
    try {
      socket = new ServerSocket(servicePort, 1, InetAddress.getByName('127.0.0.1'))
    } catch(Exception e) {
      throw new RuntimeException(e)
    }
  }

  @Override
  public void run() {
    try {
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
        socket.close()
      }
    } catch(Exception e) {
      throw new RuntimeException(e)
    }
  }
}
