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

    project.extensions.create('gretty', GrettyPluginExtension)

    project.task('thisWar', type: War, group: 'gretty', description: 'Creates thiswar.war. Could be used to configure \'slick\' WAR generation in case of WAR-overlays.') { archiveName 'thiswar.war' }

    project.afterEvaluate {

      def createConnectors = { Server jettyServer ->
        SocketConnector connector = new SocketConnector()
        // Set some timeout options to make debugging easier.
        connector.setMaxIdleTime(1000 * 60 * 60)
        connector.setSoLingerTime(-1)
        connector.setPort(project.gretty.port)
        jettyServer.setConnectors([connector ] as Connector[])
      }

      String buildWebAppFolder = "${project.buildDir}/webapp"

      project.task('prepareInplaceWebAppFolder', type: Copy, group: 'gretty', description: 'Copies webAppDir of this web-application and all WAR-overlays (if any) to ${buildDir}/webapp') {
        for(Project overlay in project.gretty.overlays) {
          from overlay.webAppDir
          into buildWebAppFolder
        }
        from project.webAppDir
        into buildWebAppFolder
      }

      if(project.gretty.overlays) {
        project.task('explodeWebApps', type: Copy, group: 'gretty', description: 'Explodes this web-application and all WAR-overlays (if any) to ${buildDir}/webapp') {
          for(Project overlay in project.gretty.overlays) {
            dependsOn overlay.tasks.war
            from overlay.zipTree(overlay.tasks.war.archivePath)
            into buildWebAppFolder
          }
          dependsOn project.tasks.thisWar
          from project.zipTree(project.tasks.thisWar.archivePath)
          into buildWebAppFolder
        }

        project.task('overlayWar', type: Zip, group: 'gretty', description: 'Creates WAR from exploded web-application in ${buildDir}/webapp') {
          dependsOn project.tasks.explodeWebApps
          from project.fileTree(buildWebAppFolder)
          destinationDir project.tasks.war.destinationDir
          archiveName project.tasks.war.archiveName
        }

        project.tasks.war {
          dependsOn project.tasks.overlayWar
          // Here we effectively turn off war task. All work is done by sequence of tasks:
          // thisWar -> explodeWebApps -> overlayWar
          rootSpec.exclude '**/*'
        }
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
        context.setContextPath contextPath ?: '/'
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
        task.dependsOn project.tasks.prepareInplaceWebAppFolder
        for(Project overlay in project.gretty.overlays)
          task.dependsOn overlay.tasks.classes
      }

      def addClassPath = { urls, proj ->
        urls.add new File(proj.buildDir, 'classes/main').toURI().toURL()
        urls.add new File(proj.buildDir, 'resources/main').toURI().toURL()
        urls.addAll proj.configurations['runtime'].collect { dep -> dep.toURI().toURL() }
      }

      def createInplaceWebAppContext = { Server jettyServer ->
        def urls = []
        addClassPath urls, project
        for(Project overlay in project.gretty.overlays.reverse())
          addClassPath urls, overlay
        URLClassLoader classLoader = new URLClassLoader(urls as URL[], GrettyPlugin.class.classLoader)
        WebAppContext context = new WebAppContext()
        setupRealm context
        setupContextPath context
        setupInitParameters context
        context.setServer jettyServer
        context.setClassLoader classLoader
        context.setResourceBase buildWebAppFolder
        jettyServer.setHandler context
      }

      def setupWarDependencies = { task ->
        task.dependsOn project.tasks.war
        // need this for stable references to ${buildDir}/webapp folder,
        // independent from presence/absence of overlays and inplace/war start mode.
        if(!project.gretty.overlays)
          task.dependsOn project.tasks.prepareInplaceWebAppFolder
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
        System.out.println 'Jetty server started.'
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
          System.out.println 'Press any key to stop the jetty server.'
        else
          System.out.println 'Enter \'gradle jettyStop\' to stop the jetty server.'
        System.out.println()
      }

      def doOnStop = {
        System.out.println 'Jetty server stopped.'
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

      project.task('jettyRun', group: 'gretty', description: 'Starts jetty server inplace, in interactive mode (keypress stops the server).') { task ->
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

      project.task('jettyRunWar', group: 'gretty', description: 'Starts jetty server on WAR-file, in interactive mode (keypress stops the server).') { task ->
        setupWarDependencies task
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

      project.task('jettyStart', group: 'gretty', description: 'Starts jetty server inplace, in batch mode (\'jettyStop\' stops the server).') { task ->
        setupInplaceWebAppDependencies task
        task.doLast {
          Server server = new Server()
          createConnectors server
          createInplaceWebAppContext server
          Thread monitor = new JettyMonitorThread(project.gretty.servicePort, server)
          monitor.start()
          server.start()
          doOnStart false
          server.join()
          doOnStop()
        }
      }

      project.task('jettyStartWar', group: 'gretty', description: 'Starts jetty server on WAR-file, in batch mode (\'jettyStop\' stops the server).') { task ->
        setupWarDependencies task
        task.doLast {
          Server server = new Server()
          createConnectors server
          createWarWebAppContext server
          Thread monitor = new JettyMonitorThread(project.gretty.servicePort, server)
          monitor.start()
          server.start()
          doOnStart false
          server.join()
          doOnStop()
        }
      }

      def sendServiceCommand = { command ->
        Socket s = new Socket(InetAddress.getByName('127.0.0.1'), project.gretty.servicePort)
        try {
          OutputStream out = s.getOutputStream()
          System.out.println "Sending command: ${command}"
          out.write(("${command}\n").getBytes())
          out.flush()
        } finally {
          s.close()
        }
      }

      project.task('jettyStop', group: 'gretty', description: 'Sends \'stop\' command to running jetty server.') { 
        doLast { sendServiceCommand 'stop' } 
      }

      project.task('jettyRestart', group: 'gretty', description: 'Sends \'restart\' command to running jetty server.') { 
        doLast { sendServiceCommand 'restart' } 
      }
    } // afterEvaluate
  } // apply
} // plugin

