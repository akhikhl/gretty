package gretty

import org.gradle.api.*
import org.gradle.api.plugins.*
import org.gradle.api.tasks.*

import org.eclipse.jetty.server.Connector
import org.eclipse.jetty.server.Server
import org.eclipse.jetty.server.bio.SocketConnector
import org.eclipse.jetty.webapp.WebAppContext

import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStream
import java.net.InetAddress
import java.net.ServerSocket
import java.net.Socket

class GrettyPlugin implements Plugin<Project> {

  private static class MonitorThread extends Thread {

    private final Server server;
    private ServerSocket socket;    

    public MonitorThread(int stopPort, final Server server) {
      this.server = server;
      setDaemon(false);
      setName("JettyServerStopMonitor");
      try {
        socket = new ServerSocket(stopPort, 1, InetAddress.getByName("127.0.0.1"));
      } catch(Exception e) {
        throw new RuntimeException(e);
      }
    }

    @Override
    public void run() {
      System.out.println("*** running jetty 'stop' thread");
      try {
        Socket accept = socket.accept();
        try {
          BufferedReader reader = new BufferedReader(new InputStreamReader(accept.getInputStream()));
          reader.readLine();
          System.out.println("*** stopping jetty embedded server");
          server.stop();
        } finally {
          accept.close();
          socket.close();
        }
      } catch(Exception e) {
        throw new RuntimeException(e);
      }
    }
  }
    
  void apply(final Project project) {
  
    project.extensions.create("gretty", GrettyPluginExtension)
    
    def doOnStart = {
      project.gretty.onStart.each { onStart ->
        if(onStart instanceof Closure)
          onStart();
      }
    }
    
    def doOnStop = {
      project.gretty.onStop.each { onStop ->
        if(onStop instanceof Closure)
          onStop();
      }
    }

    project.task("jettyRun") {
      dependsOn project.tasks.classes
      doLast {
        Server server = new Server();
        SocketConnector connector = new SocketConnector();
        
        // Set some timeout options to make debugging easier.
        connector.setMaxIdleTime(1000 * 60 * 60);
        connector.setSoLingerTime(-1);
        connector.setPort(project.gretty.port);
        server.setConnectors([ connector ] as Connector[]);
        
        def urls = [
          new File(project.buildDir, "classes/main").toURI().toURL(),
          new File(project.buildDir, "resources/main").toURI().toURL()
        ]
        urls += project.configurations["runtime"].collect { dep -> dep.toURI().toURL() }
        URLClassLoader classLoader = new URLClassLoader(urls as URL[], GrettyPlugin.class.classLoader)
             
        WebAppContext context = new WebAppContext();
        context.setServer(server);
        context.setContextPath("/");
        //context.setWar(project.tasks.war.archivePath.toString());
        context.setClassLoader(classLoader);
        context.setResourceBase(new File("$project.projectDir", "src/main/webapp").absolutePath);
     
        server.setHandler(context);
        server.start();
        System.out.println "Started jetty server on localhost:${project.gretty.port}."
        System.out.println "Press any key to stop the jetty server."
        doOnStart();
        System.out.println();
        System.in.read();
        server.stop();
        server.join();
        System.out.println "Jetty server stopped."
        doOnStop();
      }
    }

    project.task("jettyRunWar") {
      dependsOn project.tasks.war
      doLast {
        Server server = new Server();
        SocketConnector connector = new SocketConnector();
        
        // Set some timeout options to make debugging easier.
        connector.setMaxIdleTime(1000 * 60 * 60);
        connector.setSoLingerTime(-1);
        connector.setPort(project.gretty.port);
        server.setConnectors([ connector ] as Connector[]);
     
        WebAppContext context = new WebAppContext();
        context.setServer(server);
        context.setContextPath("/");
        context.setWar(project.tasks.war.archivePath.toString());
     
        server.setHandler(context);
        server.start();
        System.out.println "Started jetty server on localhost:${project.gretty.port}."
        System.out.println "Press any key to stop the jetty server."
        doOnStart();
        System.out.println();
        System.in.read();
        server.stop();
        server.join();
        System.out.println "Jetty server stopped."
        doOnStop();
      }
    }
    
    project.task("jettyStartWar") {
      dependsOn project.tasks.war
      doLast {
        Server server = new Server();
        SocketConnector connector = new SocketConnector();
     
        // Set some timeout options to make debugging easier.
        connector.setMaxIdleTime(1000 * 60 * 60);
        connector.setSoLingerTime(-1);
        connector.setPort(project.gretty.port);
        server.setConnectors([ connector ] as Connector[]);
     
        WebAppContext context = new WebAppContext();
        context.setServer(server);
        context.setContextPath("/");
        context.setWar(project.tasks.war.archivePath.toString());
     
        server.setHandler(context);
        
        Thread monitor = new MonitorThread(project.gretty.stopPort, server);
        monitor.start();
        server.start();
        System.out.println "Started jetty server on localhost:${project.gretty.port}."
        System.out.println "Enter 'gradle jettyStop' to stop the jetty server."
        doOnStart();
        System.out.println();
        server.join();
        System.out.println "Jetty server stopped."
        doOnStop();
      }
    }

    project.task("jettyStop") {
      doLast {
        Socket s = new Socket(InetAddress.getByName("127.0.0.1"), project.gretty.stopPort);
        try {      
          OutputStream out = s.getOutputStream();
          System.out.println("Sending jetty stop request");
          out.write(("\r\n").getBytes());
          out.flush();
        } finally {
          s.close();
        }
      }
    } // jettyStop task
  } // apply
} // plugin

