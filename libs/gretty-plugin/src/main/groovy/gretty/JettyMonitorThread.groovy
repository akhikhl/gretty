package gretty

final class JettyMonitorThread extends Thread {

  private final def helper
  private final def server
  private ServerSocket socket

  public JettyMonitorThread(int servicePort, def helper, def server) {
    this.helper = helper
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
            helper.stopServer server
            break
          }
          if(command == 'restart') {
            helper.stopServer server
            helper.startServer server
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
