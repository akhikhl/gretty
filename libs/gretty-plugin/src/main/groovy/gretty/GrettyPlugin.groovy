package gretty

import org.eclipse.jetty.security.HashLoginService
import org.eclipse.jetty.server.Connector
import org.eclipse.jetty.server.Server
import org.eclipse.jetty.server.bio.SocketConnector
import org.eclipse.jetty.webapp.WebAppContext
import org.gradle.api.*
import org.gradle.api.plugins.*
import org.gradle.api.tasks.*

class GrettyPlugin implements Plugin<Project> {

  private static class MonitorThread extends Thread {

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

  void apply(final Project project) {

    project.extensions.create("gretty", GrettyPluginExtension)

    def createConnectors = { Server jettyServer ->
      SocketConnector connector = new SocketConnector()
      // Set some timeout options to make debugging easier.
      connector.setMaxIdleTime(1000 * 60 * 60)
      connector.setSoLingerTime(-1)
      connector.setPort(project.gretty.port)
      jettyServer.setConnectors([connector ] as Connector[])
    }

    project.task("prepareInplaceWebApp", type: Copy) {
      from "src/main/webapp"
      into "${project.buildDir}/webapp"
    }

    def createInplaceWebAppContext = { Server jettyServer ->
      def urls = [
        new File(project.buildDir, "classes/main").toURI().toURL(),
        new File(project.buildDir, "resources/main").toURI().toURL()
      ]
      urls += project.configurations["runtime"].collect { dep -> dep.toURI().toURL() }
      URLClassLoader classLoader = new URLClassLoader(urls as URL[], GrettyPlugin.class.classLoader)
      WebAppContext context = new WebAppContext()
      if(project.gretty.realm && project.gretty.realmConfigFile) {
        File realmConfigFile = new File(project.gretty.realmConfigFile);
        if(!realmConfigFile.isAbsolute())
          realmConfigFile = new File("${project.buildDir}/webapp", realmConfigFile.path)
        context.getSecurityHandler().setLoginService(new HashLoginService(project.gretty.realm, realmConfigFile.absolutePath))
      }
      context.setServer jettyServer
      context.setContextPath project.gretty.contextPath ?: "/"
      context.setClassLoader classLoader
      context.setResourceBase "${project.buildDir}/webapp"
      jettyServer.setHandler context
    }

    def createWarWebAppContext = { Server jettyServer ->
      WebAppContext context = new WebAppContext()
      if(project.gretty.realm && project.gretty.realmConfigFile) {
        File realmConfigFile = new File(project.gretty.realmConfigFile);
        if(!realmConfigFile.isAbsolute())
          realmConfigFile = new File("${project.projectDir}/src/main/webapp", realmConfigFile.path)
        context.getSecurityHandler().setLoginService(new HashLoginService(project.gretty.realm, realmConfigFile.absolutePath))
      }
      context.setServer jettyServer
      context.setContextPath project.gretty.contextPath ?: "/"
      context.setWar project.tasks.war.archivePath.toString()
      jettyServer.setHandler context
    }

    def doOnStart = { boolean stopOnAnyKey ->
      System.out.println "Started jetty server on localhost:${project.gretty.port}."
      project.gretty.onStart.each { onStart ->
        if(onStart instanceof Closure)
          onStart()
      }
      if(stopOnAnyKey)
        System.out.println "Press any key to stop the jetty server."
      else
        System.out.println "Enter 'gradle jettyStop' to stop the jetty server."
      System.out.println()
    }

    def doOnStop = {
      System.out.println "Jetty server stopped."
      project.gretty.onStop.each { onStop ->
        if(onStop instanceof Closure)
          onStop()
      }
    }

    project.task("jettyRun") { task ->
      task.dependsOn project.tasks.classes
      task.dependsOn project.tasks.prepareInplaceWebApp
      task.doLast {
        Server server = new Server()
        createConnectors server
        createInplaceWebAppContext server
        server.start()
        doOnStart true
        System.in.read()
        server.stop()
        server.join()
        doOnStop()
      }
    }

    project.task("jettyRunWar") { task ->
      task.dependsOn project.tasks.war
      task.doLast {
        Server server = new Server()
        createConnectors server
        createWarWebAppContext server
        server.start()
        doOnStart true
        System.in.read()
        server.stop()
        server.join()
        doOnStop()
      }
    }

    project.task("jettyStart") { task ->
      task.dependsOn project.tasks.classes
      task.dependsOn project.tasks.prepareInplaceWebApp
      task.doLast {
        Server server = new Server()
        createConnectors server
        createInplaceWebAppContext server
        Thread monitor = new MonitorThread(project.gretty.stopPort, server)
        monitor.start()
        server.start()
        doOnStart false
        server.join()
        doOnStop()
      }
    }

    project.task("jettyStartWar") { task ->
      task.dependsOn project.tasks.war
      task.doLast {
        Server server = new Server()
        createConnectors server
        createWarWebAppContext server
        Thread monitor = new MonitorThread(project.gretty.stopPort, server)
        monitor.start()
        server.start()
        doOnStart false
        server.join()
        doOnStop()
      }
    }

    project.task("jettyStop") { task ->
      task.doLast {
        Socket s = new Socket(InetAddress.getByName("127.0.0.1"), project.gretty.stopPort)
        try {
          OutputStream out = s.getOutputStream()
          System.out.println "Sending jetty stop request"
          out.write(("\r\n").getBytes())
          out.flush()
        } finally {
          s.close()
        }
      }
    } // jettyStop task
  } // apply
} // plugin

