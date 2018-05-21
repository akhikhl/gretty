/*
 * Gretty
 *
 * Copyright (C) 2013-2015 Andrey Hihlovskiy and contributors.
 *
 * See the file "LICENSE" for copying and usage permission.
 * See the file "CONTRIBUTORS" for complete list of contributors.
 */
package org.akhikhl.gretty
import groovy.transform.CompileStatic
import groovy.transform.TypeCheckingMode
import org.apache.commons.io.FilenameUtils
import org.gradle.api.Project
import org.gradle.api.artifacts.ProjectDependency
import org.slf4j.Logger
import org.slf4j.LoggerFactory
/**
 *
 * @author akhikhl
 */
@CompileStatic(TypeCheckingMode.SKIP)
final class ProjectUtils {

  private static final Logger log = LoggerFactory.getLogger(ProjectUtils)

  static boolean anyWebAppUsesSpringBoot(Project project, Iterable<WebAppConfig> wconfigs) {
    wconfigs.find { wconfig ->
      isSpringBootApp(project, wconfig)
    }
  }

  private static Set collectOverlayJars(Project project) {
    Set overlayJars = new HashSet()
    def addOverlayJars // separate declaration from init to enable recursion
    addOverlayJars = { Project proj ->
      if(proj.extensions.findByName('gretty'))
        for(def overlay in proj.gretty.overlays) {
          overlay = proj.project(overlay)
          File archivePath = overlay.tasks.findByName('jar')?.archivePath
          if(archivePath)
            overlayJars.add(archivePath)
          addOverlayJars(overlay) // recursion
        }
    }
    addOverlayJars(project)
    return overlayJars
  }

  static String getContextPath(Project project) {
    String contextPath = project.gretty.contextPath
    if(!contextPath)
      for(def overlay in project.gretty.overlays.reverse()) {
        overlay = project.project(overlay)
        if(overlay.extensions.findByName('gretty')) {
          if(overlay.gretty.contextPath) {
            contextPath = overlay.gretty.contextPath
            break
          }
        } else
          log.warn 'Project {} is not gretty-enabled, could not extract it\'s context path', overlay
      }
    return contextPath
  }

  static Set<URL> getClassPath(Project project, boolean inplace, String dependencyConfigName) {
    Set<URL> urls = new LinkedHashSet()
    if(project != null && inplace) {
      addProjectClassPathWithOverlays(urls, project, dependencyConfigName)
      for(File overlayJar in collectOverlayJars(project))
        if(urls.remove(overlayJar.toURI().toURL()))
          log.debug '{} is overlay jar, exclude from classpath', overlayJar
      for(URL url in urls)
        log.debug 'classpath URL: {}', url
    }
    return urls
  }

  private static void addProjectClassPathWithOverlays(Set<URL> urls, Project proj, String dependencyConfigName) {
    addProjectClassPathWithDependencies(urls, proj, dependencyConfigName)
    // ATTENTION: order of overlay classpath is important!
    if(proj.extensions.findByName('gretty'))
      for(String overlay in proj.gretty.overlays.reverse())
        addProjectClassPathWithOverlays(urls, proj.project(overlay), dependencyConfigName)
  }

  private static void addProjectClassPathWithDependencies(Set<URL> urls, Project proj, String dependencyConfigName) {
    if(proj.sourceSets.main.output.files.any { new File(it, 'META-INF/web-fragment.xml').exists() })
      urls.add proj.jar.archivePath.toURI().toURL()
    else
      urls.addAll proj.sourceSets.main.output.files.collect { it.toURI().toURL() }
    def dependencyConfig = proj.configurations.findByName(dependencyConfigName)
    if(dependencyConfig) {
      def files = dependencyConfig.fileCollection { !(it instanceof ProjectDependency) }
      def grettyProvidedCompileConfig = proj.configurations.findByName('grettyProvidedCompile')
      if(grettyProvidedCompileConfig)
        files = files - grettyProvidedCompileConfig
      urls.addAll(files.collect { it.toURI().toURL() })
      dependencyConfig.allDependencies.withType(ProjectDependency).each { dep ->
        addProjectClassPathWithDependencies(urls, dep.getDependencyProject(), dependencyConfigName)
      }
    }
  }

  static Set<URL> getClassPathJars(Project project, String dependencyConfig) {
    Set<URL> urls = new LinkedHashSet()
    if(project != null) {
      def addProjectClassPath
      addProjectClassPath = { Project proj ->
        urls.addAll(project.tasks.jar.archivePath.toURI().toURL())
        def files = proj.configurations[dependencyConfig]
        def grettyProvidedCompileConfig = proj.configurations.findByName('grettyProvidedCompile')
        if(grettyProvidedCompileConfig)
          files = files - grettyProvidedCompileConfig
        urls.addAll(files.files.collect { it.toURI().toURL() })
        // ATTENTION: order of overlay classpath is important!
        if(proj.extensions.findByName('gretty'))
          for(String overlay in proj.gretty.overlays.reverse())
            addProjectClassPath(proj.project(overlay))
      }
      addProjectClassPath(project)
      for(File overlayJar in collectOverlayJars(project))
        if(urls.remove(overlayJar.toURI().toURL()))
          log.debug '{} is overlay jar, exclude from classpath', overlayJar
      for(URL url in urls)
        log.debug 'classpath URL: {}', url
    }
    return urls
  }

  static ServerConfig getDefaultServerConfig(Project project) {
    ServerConfig.getDefaultServerConfig(project.name)
  }

  static WebAppConfig getDefaultWebAppConfigForProject(Project project) {
    WebAppConfig result = new WebAppConfig()
    result.contextPath = '/' + project.name
    result.fastReload = true
    result.recompileOnSourceChange = true
    result.reloadOnClassChange = true
    result.reloadOnConfigChange = true
    result.reloadOnLibChange = true
    result.resourceBase = {
      def resourceBase
      if(inplace) {
          if(inplaceMode == 'hard') {
              // in hard inplaceMode resourceBase is src/main/webapp (or something user specified instead)
              resourceBase = ProjectUtils.getWebAppDir(project).toString()
          } else {
              resourceBase = "${project.buildDir}/inplaceWebapp/" as String
          }
      } else {
          resourceBase = ProjectUtils.getFinalArchivePath(project).toString()
      }
      return resourceBase
    }
    result.projectPath = project.path
    return result
  }

  static String getFarmConfigurationName(farmName) {
    return 'farm' + farmName
  }

  static File getFileFromConfiguration(Project project, String configurationName, String dependency) {
    def gav = dependency.split(':')
    def artifact = project.configurations[configurationName].resolvedConfiguration.resolvedArtifacts.find {
      it.moduleVersion.id.group == gav[0] && it.moduleVersion.id.name == gav[1]
    }
    artifact.file
  }

  static WebAppConfig getDefaultWebAppConfigForMavenDependency(Project project, String farmName, String dependency, List<String> dependencies = []) {
    WebAppConfig result = new WebAppConfig()
    result.contextPath = '/' + dependency.split(':')[1]
    String configurationName = getFarmConfigurationName(farmName)
    result.resourceBase = {
      getFileFromConfiguration(project, configurationName, dependency).absolutePath
    }
    if(dependencies) {
      result.classPath = dependencies.collect {
        getFileFromConfiguration(project, configurationName, it).absolutePath
      } as Set<String>
    }
    return result
  }

  static File getFinalArchivePath(Project project) {
    project.ext.properties.containsKey('finalArchivePath') ? project.ext.finalArchivePath : (project.tasks.findByName('war') ?: project.tasks.jar).archivePath
  }

  static File getWebAppDir(Project project) {
    project.hasProperty('webAppDir') ? project.webAppDir : new File(project.projectDir, 'src/main/webapp')
  }

  static Set<File> getWebAppDirs(Project project) {
    Set files = new LinkedHashSet()
    if(project != null) {
      def addWebAppDir
      addWebAppDir = { Project proj ->
        // ATTENTION: order of overlay classpath is important!
        if(proj.extensions.findByName('gretty'))
          for(String overlay in proj.gretty.overlays)
            addWebAppDir(proj.project(overlay))
        files.add getWebAppDir(proj)
      }
      addWebAppDir(project)
    }
    return files
  }

  static String getWebAppDestinationDirName(Project project, WebAppConfig wconfig) {
    if(wconfig.projectPath)
      project.project(wconfig.projectPath).name
    else {
      def file = wconfig.resourceBase
      if(!(file instanceof File))
        file = new File(file.toString())
      FilenameUtils.getBaseName(file.name).replaceAll(/([\da-zA-Z_.-]+?)-((\d+\.)+[\da-zA-Z_.-]*)/, '$1')
    }
  }

  static File getWebInfDir(Project project) {
    new File(getWebAppDir(project), 'WEB-INF')
  }

  // ATTENTION: this function resolves compile configuration!
  static boolean isSpringBootApp(Project project) {
    def compileConfig = project.configurations.findByName('compileOnly')
    compileConfig && compileConfig.resolvedConfiguration.resolvedArtifacts.find { it.moduleVersion.id.group == 'org.springframework.boot' }
  }

  static boolean isSpringBootApp(Project project, WebAppConfig wconfig) {
    wconfig.springBoot || (wconfig.projectPath && isSpringBootApp(project.project(wconfig.projectPath)))
  }

  static void prepareExplodedWebAppFolder(Project project) {
    // ATTENTION: overlay copy order is important!
    for(String overlay in project.gretty.overlays) {
      def overlayProject = project.project(overlay)
      project.copy {
        from overlayProject.zipTree(getFinalArchivePath(overlayProject))
        into "${project.buildDir}/explodedWebapp"
      }
    }
    project.copy {
      from project.zipTree((project.tasks.findByName('war') ?: project.tasks.jar).archivePath)
      into "${project.buildDir}/explodedWebapp"
    }
  }

  static void prepareExplodedFarmWebAppFolder(Project project, File war, List<String> overlays, String targetFolder) {
    for(String overlay in overlays) {
      def overlayProject = project.project(overlay)
      project.copy {
        from overlayProject.zipTree(getFinalArchivePath(overlayProject))
        into targetFolder
      }
    }
    project.copy {
      from project.zipTree(war)
      into targetFolder
    }
  }

  static void prepareInplaceWebAppFolder(Project project) {
    new File(project.buildDir, 'inplaceWebapp').mkdirs()
    // ATTENTION: overlay copy order is important!
    for(String overlay in project.gretty.overlays) {
      Project overlayProject = project.project(overlay)
      prepareInplaceWebAppFolder(overlayProject)
      project.copy {
        from "${overlayProject.buildDir}/inplaceWebapp"
        into "${project.buildDir}/inplaceWebapp"
      }
    }
    new File(project.buildDir, 'inplaceWebapp').mkdirs()
    project.copy {
      from getWebAppDir(project), project.gretty.webappCopy
      into "${project.buildDir}/inplaceWebapp"
    }
  }

  static void prepareToRun(Project project, WebAppConfig wconfig) {
    wconfig.prepareToRun()
    if(!wconfig.springBootMainClass && wconfig.projectPath && isSpringBootApp(project, wconfig))
      wconfig.springBootMainClass = SpringBootMainClassFinder.findMainClass(project.project(wconfig.projectPath))
  }

  static Set<URL> resolveClassPath(Project project, Collection classPath) {
    def resolvedClassPath = new LinkedHashSet()
    if(classPath == null)
      return resolvedClassPath
    for(def elem in classPath) {
      while(elem instanceof Closure)
        elem = elem()
      if(elem != null) {
        if(elem instanceof File) {
          if(!elem.isAbsolute()) {
            if(project == null)
              elem = new File(System.getProperty('user.home'), elem.path).absolutePath
            else
              elem = new File(project.projectDir, elem.path)
          }
          elem = elem.toURI().toURL()
        }
        else if(elem instanceof URI)
          elem = elem.toURL()
        else if(!(elem instanceof URL)) {
          elem = elem.toString()
          if(!(elem =~ /.{2,}\:.+/)) { // no schema?
            if(!new File(elem).isAbsolute()) {
              if(project == null)
                elem = new File(System.getProperty('user.home'), elem).absolutePath
              else
                elem = new File(project.projectDir, elem).absolutePath
            }
            if(!elem.startsWith('/'))
              elem = '/' + elem
            elem = 'file://' + elem
          }
          elem = new URL(elem.toString())
        }
        resolvedClassPath.add(elem)
      }
    }
    resolvedClassPath
  }

  static void resolveServerConfig(Project project, ServerConfig sconfig) {

    def resolvedJvmArgs = []
    for(def arg in sconfig.jvmArgs) {
      while(arg instanceof Closure)
        arg = arg()
      if(arg != null)
        resolvedJvmArgs.add(arg.toString())
    }
    sconfig.jvmArgs = resolvedJvmArgs

    if(!(sconfig.sslKeyStorePath instanceof String) || !sconfig.sslKeyStorePath.startsWith('classpath:'))
      sconfig.sslKeyStorePath = new FileResolver(['security', 'config', '.']).resolveSingleFile(project, sconfig.sslKeyStorePath)

    if(!(sconfig.sslTrustStorePath instanceof String) || !sconfig.sslTrustStorePath.startsWith('classpath:'))
      sconfig.sslTrustStorePath = new FileResolver(['security', 'config', '.']).resolveSingleFile(project, sconfig.sslTrustStorePath)

    String servletContainerType = ServletContainerConfig.getConfig(sconfig.servletContainer).servletContainerType

    def realmConfigFiles = [ sconfig.realmConfigFile ]
    if(servletContainerType == 'tomcat') {
      realmConfigFiles.add(sconfig.servletContainer + '-users.xml')
      realmConfigFiles.add('tomcat-users.xml')
      if(sconfig.realmConfigFile) {
        def f = sconfig.realmConfigFile
        if(!(f instanceof File))
          f = new File(f.toString())
        realmConfigFiles.add(new File(f, sconfig.servletContainer + '-users.xml'))
        realmConfigFiles.add(new File(f, 'tomcat-users.xml'))
      }
    } else if(servletContainerType == 'jetty') {
      realmConfigFiles.add(sconfig.servletContainer + '-realm.properties')
      realmConfigFiles.add('jetty-realm.properties')
      if(sconfig.realmConfigFile) {
        def f = sconfig.realmConfigFile
        if(!(f instanceof File))
          f = new File(f.toString())
        realmConfigFiles.add(new File(f, sconfig.servletContainer + '-realm.properties'))
        realmConfigFiles.add(new File(f, 'jetty-realm.properties'))
      }
    }
    realmConfigFiles = realmConfigFiles as LinkedHashSet
    sconfig.realmConfigFile = new FileResolver(['realm', 'security', 'server', 'config', '.']).resolveSingleFile(project, realmConfigFiles)

    def specifiedServerConfigFile = sconfig.serverConfigFile
    if(servletContainerType == 'jetty') {
      def serverConfigFiles = (specifiedServerConfigFile ? [ specifiedServerConfigFile ] : [ sconfig.servletContainer + '.xml', 'jetty.xml' ]) as LinkedHashSet
      sconfig.serverConfigFile = new FileResolver(['jetty', 'server', 'config', '.']).resolveSingleFile(project, serverConfigFiles)
    } else if(servletContainerType == 'tomcat') {
      def serverConfigFiles = (specifiedServerConfigFile ? [ specifiedServerConfigFile ] : [ sconfig.servletContainer + '.xml', 'tomcat.xml', sconfig.servletContainer + '-server.xml', 'server.xml' ]) as LinkedHashSet
      sconfig.serverConfigFile = new FileResolver(['tomcat', 'server', 'config', '.']).resolveSingleFile(project, serverConfigFiles)
    } else
      sconfig.serverConfigFile = null
    if(sconfig.serverConfigFile == null && specifiedServerConfigFile)
      log.warn 'server configuration file {} is not found', specifiedServerConfigFile.toString()

    def specifiedLogbackConfigFile = sconfig.logbackConfigFile
    def logbackConfigFiles = ( specifiedLogbackConfigFile ? [ specifiedLogbackConfigFile ] : [ 'logback.groovy', 'logback.xml' ]) as LinkedHashSet
    sconfig.logbackConfigFile = new FileResolver(
      ['logback', 'server', 'config', '.' ]
    ).resolveSingleFile(project, logbackConfigFiles)
    if(sconfig.logbackConfigFile == null && specifiedLogbackConfigFile)
      log.warn 'logback configuration file {} is not found', specifiedLogbackConfigFile.toString()
  }

  static void resolveWebAppConfig(Project project, WebAppConfig wconfig, ServerConfig sconfig) {

    if(sconfig == null)
      return

    String servletContainerType = ServletContainerConfig.getConfig(sconfig.servletContainer).servletContainerType

    if(wconfig.extraResourceBases) {
      def resolver = new FileResolver(['.'])
      resolver.acceptFiles = false
      resolver.acceptDirs = true
      wconfig.extraResourceBases = wconfig.extraResourceBases.findResults { resolver.resolveSingleFile(project, it) }
    }

    def realmConfigFiles = [ wconfig.realmConfigFile ]
    if(servletContainerType == 'tomcat') {
      realmConfigFiles.add(sconfig.servletContainer + '-users.xml')
      realmConfigFiles.add('tomcat-users.xml')
      if(wconfig.realmConfigFile) {
        def f = wconfig.realmConfigFile
        if(!(f instanceof File))
          f = new File(f.toString())
        realmConfigFiles.add(new File(f, sconfig.servletContainer + '-users.xml'))
        realmConfigFiles.add(new File(f, 'tomcat-users.xml'))
      }
    } else if(servletContainerType == 'jetty') {
      realmConfigFiles.add(sconfig.servletContainer + '-realm.properties')
      realmConfigFiles.add('jetty-realm.properties')
      if(wconfig.realmConfigFile) {
        def f = wconfig.realmConfigFile
        if(!(f instanceof File))
          f = new File(f.toString())
        realmConfigFiles.add(new File(f, sconfig.servletContainer + '-realm.properties'))
        realmConfigFiles.add(new File(f, 'jetty-realm.properties'))
      }
    }
    realmConfigFiles = realmConfigFiles as LinkedHashSet
    wconfig.realmConfigFile = new FileResolver(['webapp-realm', 'webapp-security', 'webapp-config']).resolveSingleFile(project, realmConfigFiles)

    if(servletContainerType == 'jetty') {
      def contextConfigFiles = [ wconfig.contextConfigFile, sconfig.servletContainer + '-env.xml', 'jetty-env.xml' ] as LinkedHashSet
      wconfig.contextConfigFile = new FileResolver(['webapp-jetty', 'webapp-config' ]).resolveSingleFile(project, contextConfigFiles)
    } else if(servletContainerType == 'tomcat') {
      def contextConfigFiles = [ wconfig.contextConfigFile, sconfig.servletContainer + '-context.xml', 'tomcat-context.xml', 'context.xml' ] as LinkedHashSet
      wconfig.contextConfigFile = new FileResolver(['webapp-tomcat', 'webapp-config' ]).resolveSingleFile(project, contextConfigFiles)
    } else
      wconfig.contextConfigFile = null
  }

  static List<Project> getDependencyProjects(Project project, String configurationName) {
    project.configurations[configurationName].dependencies.withType(ProjectDependency).collect{ it.dependencyProject }
  }

  /**
   * Returns set of projects: this project and all it's overlays.
   */
  static Set<Project> withOverlays(Project project) {
    Set projects = new HashSet()
    projects.add(project)
    def addOverlays // separate declaration from init to enable recursion
    addOverlays = { Project proj ->
      if(proj.extensions.findByName('gretty'))
        for(def overlay in proj.gretty.overlays) {
          overlay = proj.project(overlay)
          if(overlay)
            projects.add(overlay)
          addOverlays(overlay) // recursion
        }
    }
    addOverlays(project)
    return projects
  }
}
