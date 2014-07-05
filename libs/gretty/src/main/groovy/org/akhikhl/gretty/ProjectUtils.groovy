/*
 * gretty
 *
 * Copyright 2013  Andrey Hihlovskiy.
 *
 * See the file "license.txt" for copying and usage permission.
 */
package org.akhikhl.gretty

import java.util.regex.Pattern
import org.apache.commons.io.FilenameUtils
import org.gradle.api.Project
import org.gradle.api.file.FileCollection
import org.gradle.api.file.FileTree
import org.slf4j.Logger
import org.slf4j.LoggerFactory

final class ProjectUtils {

  private static final Logger log = LoggerFactory.getLogger(ProjectUtils)

  private static void addDefaultFastReloadDirs(List<FastReloadStruct> result, Project proj) {
    result.add(new FastReloadStruct(baseDir: getWebAppDir(proj)))
    for(def overlay in proj.gretty.overlays)
      addDefaultFastReloadDirs(result, proj.project(overlay))
  }

  private static void addFastReloadDirs(List<FastReloadStruct> result, Project proj, List fastReloads) {
    for(def f in fastReloads) {
      if(f instanceof Boolean)
        continue
      File baseDir
      def pattern
      def excludesPattern
      if(f instanceof String)
        baseDir = new File(f)
      else if(f instanceof File)
        baseDir = f
      else if(f instanceof Map) {
        f.each { key, value ->
          if(key == 'baseDir')
            baseDir = value instanceof File ? value : new File(value.toString())
          else if(key == 'pattern')
            pattern = value
          else if(key == 'excludesPattern')
            excludesPattern = value
          else
            log.warn 'Unknown fastReload property: {}', key
        }
        if(!baseDir) {
          log.warn 'fastReload property baseDir is not specified'
          continue
        }
      } else {
        log.warn 'fastReload argument must be String, File or Map'
        continue
      }
      resolveFile(proj, baseDir).each {
        result.add(new FastReloadStruct(baseDir: it, pattern: pattern, excludesPattern: excludesPattern))
      }
    }
  }

  static boolean anyWebAppUsesSpringBoot(Project project, Iterable<WebAppConfig> wconfigs) {
    wconfigs.find { wconfig ->
      isSpringBootApp(project, wconfig)
    }
  }

  private static void collectFastReloads(List result, Project proj) {
    if(proj.gretty.fastReload != null)
      result.addAll(proj.gretty.fastReload)
    for(def overlay in proj.gretty.overlays.reverse()) {
      overlay = proj.project(overlay)
      if(overlay.extensions.findByName('gretty'))
        collectFastReloads(result, overlay)
    }
  }

  static List<File> collectFilesInOutput(Project project, Object filePattern, boolean searchOverlays = true) {
    List<File> result = []
    def collectIt
    collectIt = { Project proj ->
      if(proj.hasProperty('sourceSets'))
        proj.sourceSets.main.output.files.each { File sourceDir ->
          def collectInDir
          collectInDir = { File dir ->
            dir.listFiles().each {
              if(it.isFile()) {
                String relPath = it.absolutePath - sourceDir.absolutePath - '/'
                boolean match = (filePattern instanceof java.util.regex.Pattern) ? (relPath =~ filePattern) : (relPath == filePattern)
                if(match)
                  result.add(it)
              }
              else
                collectInDir(it)
            }
          }
          collectInDir(sourceDir)
        }
      if(searchOverlays && proj.extensions.findByName('gretty'))
        for(String overlay in proj.gretty.overlays.reverse())
          collectIt(proj.project(overlay))
    }
    collectIt(project)
    log.debug 'collectFilesInOutput filePattern: {}, result: {}', filePattern, result
    return result
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

  static File findFileInOutput(Project project, Object filePattern, boolean searchOverlays = true) {
    List files = collectFilesInOutput(project, filePattern, searchOverlays)
    return files.isEmpty() ? null : files[0]
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

  static Set<URL> getClassPath(Project project, boolean inplace, String dependencyConfig) {
    Set<URL> urls = new LinkedHashSet()
    if(project != null && inplace) {
      def addProjectClassPath
      addProjectClassPath = { Project proj ->
        urls.addAll proj.sourceSets.main.output.files.collect { it.toURI().toURL() }
        urls.addAll proj.configurations[dependencyConfig].files.collect { it.toURI().toURL() }
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

  static Set<URL> getClassPathJars(Project project, String dependencyConfig) {
    Set<URL> urls = new LinkedHashSet()
    if(project != null) {
      def addProjectClassPath
      addProjectClassPath = { Project proj ->
        urls.addAll project.tasks.jar.archivePath.toURI().toURL()
        urls.addAll proj.configurations[dependencyConfig].files.collect { it.toURI().toURL() }
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
    result.realmConfigFile = null
    result.jettyEnvXmlFile = 'jetty-env.xml'
    result.fastReload = true
    result.inplaceResourceBase = "${project.buildDir}/inplaceWebapp/" as String
    result.warResourceBase = ProjectUtils.getFinalArchivePath(project).toString()
    result.projectPath = project.path
    return result
  }

  static WebAppConfig getDefaultWebAppConfigForMavenDependency(Project project, String dependency) {
    WebAppConfig result = new WebAppConfig()
    result.contextPath = '/' + dependency.split(':')[1]
    result.warResourceBase = {
      def gav = dependency.split(':')
      def artifact = project.configurations.farm.resolvedConfiguration.resolvedArtifacts.find { it.moduleVersion.id.group == gav[0] && it.moduleVersion.id.name == gav[1] }
      artifact.file.absolutePath
    }
    result.inplace = false
    return result
  }

  static List<FastReloadStruct> getFastReload(Project project, List fastReloads = null) {
    if(fastReloads == null)
      fastReloads = []
    collectFastReloads(fastReloads, project)
    List<FastReloadStruct> result = []
    if(fastReloads.find { (it instanceof Boolean) && it })
      addDefaultFastReloadDirs(result, project)
    addFastReloadDirs(result, project, fastReloads)
    log.debug '{} fastReload: {}', project, result
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
      def warFile = wconfig.warResourceBase
      if(!(warFile instanceof File))
        warFile = new File(warFile.toString())
      FilenameUtils.getBaseName(warFile.name).replaceAll(/([\da-zA-Z_.-]+?)-((\d+\.)+[\da-zA-Z_.-]*)/, '$1')
    }
  }

  // ATTENTION: this function resolves compile configuration!
  static boolean isSpringBootApp(Project project) {
    project.configurations.compile.resolvedConfiguration.resolvedArtifacts.find { it.moduleVersion.id.group == 'org.springframework.boot' }
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

  static void prepareInplaceWebAppFolder(Project project) {
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
      from getWebAppDir(project)
      into "${project.buildDir}/inplaceWebapp"
    }
  }
  
  static void prepareToRun(Project project, WebAppConfig wconfig) {
    wconfig.prepareToRun()
    Set springBootSources = new LinkedHashSet()
    if(wconfig.springBootSources) {
      if(wconfig.springBootSources instanceof Collection)
        springBootSources += wconfig.springBootSources
      else
        springBootSources += wconfig.springBootSources.toString().split(',').collect { it.trim() }
    }
    if(wconfig.projectPath && wconfig.projectPath != project.path && isSpringBootApp(project, wconfig)) {
      String mainClass = SpringBootMainClassFinder.findMainClass(project.project(wconfig.projectPath))
      if(mainClass && mainClass.contains('.'))
        springBootSources += mainClass.substring(0, mainClass.lastIndexOf('.'))
    }
    wconfig.springBootSources = springBootSources.join(',')
  }

  static Set<URL> resolveClassPath(Project project, Collection<URL> classPath) {
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

    sconfig.sslKeyStorePath = resolveSingleFile(project, sconfig.sslKeyStorePath)
    sconfig.sslTrustStorePath = resolveSingleFile(project, sconfig.sslTrustStorePath)

    def f = resolveSingleFile(project, sconfig.jettyXmlFile)
    if(f == null && sconfig.jettyXmlFile) {
      def f2 = sconfig.jettyXmlFile
      if(!(f2 instanceof File))
        f2 = new File(f2)
      if(!f2.isAbsolute()) {
        String jettyHome = System.getenv('JETTY_HOME')
        if(!jettyHome)
          jettyHome = System.getProperty('jetty.home')
        if(jettyHome != null) {
          File f3 = new File(new File(jettyHome, 'etc'), f2.path)
          if(f3.exists())
            f = f3
        }
      }
    }
    sconfig.jettyXmlFile = f

    f = ProjectUtils.resolveSingleFile(project, sconfig.logbackConfigFile)
    if(f == null && sconfig.logbackConfigFile != 'logback.groovy')
      f = ProjectUtils.resolveSingleFile(project, 'logback.groovy')
    if(f == null && sconfig.logbackConfigFile != 'logback.xml')
      f = ProjectUtils.resolveSingleFile(project, 'logback.xml')
    sconfig.logbackConfigFile = f
  }

  static void resolveWebAppConfig(Project project, WebAppConfig wconfig, String servletContainer) {

    String servletContainerType = ServletContainerConfig.getConfig(servletContainer).servletContainerType

    boolean defaultRealmConfigFile = false
    if(!wconfig.realmConfigFile) {
      defaultRealmConfigFile = true
      if(servletContainerType == 'tomcat')
        wconfig.realmConfigFile = 'tomcat-users.xml'
      else
        wconfig.realmConfigFile = 'jetty-realm.properties'
    }

    if(servletContainerType == 'jetty' && !defaultRealmConfigFile && !wconfig.realm)
      log.warn 'Realm config file is specified, but realm is not specified.'

    def resolvedRealmConfigFile = ProjectUtils.resolveSingleFile(project, wconfig.realmConfigFile)
    if(!resolvedRealmConfigFile && !defaultRealmConfigFile)
      log.warn 'The realm config file {} does not exist.', wconfig.realmConfigFile
    wconfig.realmConfigFile = resolvedRealmConfigFile

    if(servletContainerType == 'jetty')
      wconfig.jettyEnvXmlFile = ProjectUtils.resolveSingleFile(project, wconfig.jettyEnvXmlFile)
    else
      wconfig.jettyEnvXmlFile = null
  }

  static List<File> resolveFile(Project project, file) {
    List<File> result = []
    if(file != null) {
      resolveFile_(result, project, file)
      if(result.isEmpty())
        log.debug 'Could not resolve file \'{}\' in {}', file, project
    }
    return result
  }

  private static void resolveFile_(List<File> result, Project project, file) {
    if(!(file instanceof File))
      file = new File(file)
    if(file.isAbsolute()) {
      result.add(file.absoluteFile)
      return
    }
    if(project != null) {
      File f = new File(project.projectDir, file.path)
      if(f.exists())
        result.add(f.absoluteFile)
      f = new File(new File(getWebAppDir(project), 'WEB-INF'), file.path)
      if(f.exists())
        result.add(f.absoluteFile)
      result.addAll(collectFilesInOutput(project, file.path, false))
      if(project.extensions.findByName('gretty'))
        for(def overlay in project.gretty.overlays.reverse())
          resolveFile_(result, project.project(overlay), file)
    }
  }

  static File resolveSingleFile(Project project, file) {
    List<File> list = resolveFile(project, file)
    list ? list[0] : null
  }
}
