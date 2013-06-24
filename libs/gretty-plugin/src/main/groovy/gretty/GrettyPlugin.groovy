package gretty

import org.eclipse.jetty.security.HashLoginService
import org.eclipse.jetty.server.Connector
import org.eclipse.jetty.server.Server
import org.eclipse.jetty.server.bio.SocketConnector
import org.eclipse.jetty.webapp.WebAppContext
import org.gradle.api.*
import org.gradle.api.plugins.*
import org.gradle.api.tasks.*
import org.gradle.api.tasks.bundling.*

class GrettyPlugin implements Plugin<Project> {

  void apply(final Project project) {

    project.extensions.create("gretty", GrettyPluginExtension)

    project.afterEvaluate {

      def createConnectors = { Server jettyServer ->
        SocketConnector connector = new SocketConnector()
        // Set some timeout options to make debugging easier.
        connector.setMaxIdleTime(1000 * 60 * 60)
        connector.setSoLingerTime(-1)
        connector.setPort(project.gretty.port)
        jettyServer.setConnectors([connector ] as Connector[])
      }

      project.task("prepareInplaceWebApp", type: Copy) {
        for(Project overlay in project.gretty.overlays) {
          from overlay.webAppDir
          into "${project.buildDir}/webapp"
        }
        from project.webAppDir
        into "${project.buildDir}/webapp"
      }

      def setupRealm = { WebAppContext context ->
        String realm = project.gretty.realm
        String realmConfigFile = project.gretty.realmConfigFile
        if(realmConfigFile && !new File(realmConfigFile).isAbsolute())
          realmConfigFile = "${project.webAppDir.absolutePath}/${realmConfigFile}"
        if(!realm || !realmConfigFile)
          for(Project overlay in project.gretty.overlays.reverse())
            if(overlay.gretty.realm && overlay.gretty.realmConfigFile) {
              realm = overlay.gretty.realm
              realmConfigFile = overlay.gretty.realmConfigFile
              if(realmConfigFile && !new File(realmConfigFile).isAbsolute())
                realmConfigFile = "${overlay.webAppDir.absolutePath}/${realmConfigFile}"
              break
            }
        if(realm && realmConfigFile)
          context.getSecurityHandler().setLoginService(new HashLoginService(realm, realmConfigFile))
      }

      def setupContextPath = { WebAppContext context ->
        String contextPath = project.gretty.contextPath
        if(!contextPath)
          for(Project overlay in project.gretty.overlays.reverse())
            if(overlay.gretty.contextPath) {
              contextPath = overlay.gretty.contextPath
              break
            }
        context.setContextPath contextPath ?: "/"
      }

      def setupInitParameters = { WebAppContext context ->
        for(Project overlay in project.gretty.overlays)
          for(def e in overlay.gretty.initParameters) {
            def paramValue = e.value
            if(paramValue instanceof Closure)
              paramValue = paramValue()
            context.setInitParameter e.key, paramValue
          }
        for(def e in project.gretty.initParameters) {
          def paramValue = e.value
          if(paramValue instanceof Closure)
            paramValue = paramValue()
          context.setInitParameter e.key, paramValue
        }
      }

      def setupInplaceWebAppDependencies = { task ->
        task.dependsOn project.tasks.classes
        task.dependsOn project.tasks.prepareInplaceWebApp
        for(Project overlay in project.gretty.overlays)
          task.dependsOn overlay.tasks.classes
      }

      def createInplaceWebAppContext = { Server jettyServer ->
        def urls = [
          new File(project.buildDir, "classes/main").toURI().toURL(),
          new File(project.buildDir, "resources/main").toURI().toURL()
        ]
        urls += project.configurations["runtime"].collect { dep -> dep.toURI().toURL() }
        for(Project overlay in project.gretty.overlays.reverse()) {
          urls.add new File(overlay.buildDir, "classes/main").toURI().toURL()
          urls.add new File(overlay.buildDir, "resources/main").toURI().toURL()
          urls += overlay.configurations["runtime"].collect { dep -> dep.toURI().toURL() }
        }
        URLClassLoader classLoader = new URLClassLoader(urls as URL[], GrettyPlugin.class.classLoader)
        WebAppContext context = new WebAppContext()
        setupRealm context
        setupContextPath context
        setupInitParameters context
        context.setServer jettyServer
        context.setClassLoader classLoader
        context.setResourceBase "${project.buildDir}/webapp"
        jettyServer.setHandler context
      }

      def createWarWebAppContext = { Server jettyServer ->
        WebAppContext context = new WebAppContext()
        setupRealm context
        setupContextPath context
        setupInitParameters context
        context.setServer jettyServer
        context.setWar project.tasks.war.archivePath.toString()
        jettyServer.setHandler context
      }

      def doOnStart = { boolean interactive ->
        System.out.println "Jetty server started."
        System.out.println 'You can see web-application in browser under the address:'
        System.out.println "http://localhost:${project.gretty.port}${project.gretty.contextPath}"
        for(Project overlay in project.gretty.overlays)
          overlay.gretty.onStart.each { onStart ->
            if(onStart instanceof Closure)
              onStart()
          }
        project.gretty.onStart.each { onStart ->
          if(onStart instanceof Closure)
            onStart()
        }
        if(interactive)
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
        for(Project overlay in project.gretty.overlays.reverse())
          overlay.gretty.onStop.each { onStop ->
            if(onStop instanceof Closure)
              onStop()
          }
      }

      if(project.gretty.overlays) {
        def explodedWebAppDir = "${project.buildDir}/explodedWebApp"

        project.task("thisWar", type: War) { archiveName "thiswar.war" }

        project.task("explodeWebApps", type: Copy) {
          for(Project overlay in project.gretty.overlays) {
            dependsOn overlay.tasks.war
            from overlay.zipTree(overlay.tasks.war.archivePath)
            into explodedWebAppDir
          }
          dependsOn project.tasks.thisWar
          from project.zipTree(project.tasks.thisWar.archivePath)
          into explodedWebAppDir
        }

        project.task("overlayWar", type: Zip) {
          dependsOn project.tasks.explodeWebApps
          destinationDir project.tasks.war.destinationDir
          archiveName project.tasks.war.archiveName
          from project.fileTree(explodedWebAppDir)
        }

        project.tasks.war {
          dependsOn project.tasks.overlayWar
          rootSpec.exclude '**/*'
        }
      }

      project.task("jettyRun") { task ->
        setupInplaceWebAppDependencies task
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
        setupInplaceWebAppDependencies task
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
    } // afterEvaluate
  } // apply
} // plugin

