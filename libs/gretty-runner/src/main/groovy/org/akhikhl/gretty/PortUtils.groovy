package org.akhikhl.gretty

import groovy.transform.CompileStatic
import groovy.transform.TypeCheckingMode

@CompileStatic(TypeCheckingMode.SKIP)
class PortUtils {

  // attention: this constant must always have the same value as ServerConfig.RANDOM_FREE_PORT
  static final int RANDOM_FREE_PORT = -1

  static int findFreePort() {
    int[] ports = findFreePorts(1)
    if(ports.length == 0)
      throw new Exception("Could not find free port")
    ports[0]
  }

  static int[] findFreePorts(int count, List<Integer> range = null) {
    List result = []
    try {
      List sockets = []
      try {
        if(!range) {
          while(count-- > 0) {
            ServerSocket socket = new ServerSocket(0)
            sockets.add(socket)
            result.add(socket.getLocalPort())
          }
        } else {
          for(Integer port in range) {
            try {
              ServerSocket socket = new ServerSocket(port)
              sockets.add(socket)
              result.add(socket.getLocalPort())
              if(--count == 0) {
                break;
              }
            } catch (IOException io) { }
          }
          if(count > 0) {
            throw new IllegalStateException("Unable to find enough ports");
          }
        }
      } finally {
        for(ServerSocket socket in sockets)
          socket.close()
      }
    } catch (IOException e) {
    }
    return result as int[]
  }
}
