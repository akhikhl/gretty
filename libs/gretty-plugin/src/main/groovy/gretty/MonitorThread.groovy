package gretty

import org.eclipse.jetty.server.Server

final class MonitorThread extends Thread {

  private final Server server
  private ServerSocket socket

  public MonitorThread(int stopPort, final Server server) {
    this.server = server
    setDaemon(false)
    setName("JettyServerStopMonitor")
    try {
      socket = new ServerSocket(stopPort, 1, InetAddress.getByName("127.0.0.1"))
    } catch(Exception e) {
      throw new RuntimeException(e)
    }
  }

  @Override
  public void run() {
    try {
      Socket accept = socket.accept()
      try {
        BufferedReader reader = new BufferedReader(new InputStreamReader(accept.getInputStream()))
        reader.readLine()
        server.stop()
      } finally {
        accept.close()
        socket.close()
      }
    } catch(Exception e) {
      throw new RuntimeException(e)
    }
  }
}
