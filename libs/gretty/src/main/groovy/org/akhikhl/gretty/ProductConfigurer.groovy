/*
/*
 * Gretty
 *
 * Copyright (C) 2013-2015 Andrey Hihlovskiy and contributors.
 *
 * See the file "LICENSE" for copying and usage permission.
 * See the file "CONTRIBUTORS" for complete list of contributors.
 */
package org.akhikhl.gretty

import groovy.json.JsonBuilder
import groovy.transform.CompileStatic
import groovy.transform.TypeCheckingMode
import org.gradle.api.Project
import org.gradle.api.file.FileCollection
import org.gradle.api.tasks.bundling.Zip
import org.slf4j.Logger
import org.slf4j.LoggerFactory

/**
 *
 * @author akhikhl
 */
@CompileStatic(TypeCheckingMode.SKIP)
class ProductConfigurer {

  protected static final Logger log = LoggerFactory.getLogger(ProductConfigurer)

  protected static final String mainClass = 'org.akhikhl.gretty.GrettyStarter'

  protected final Project project
  protected final File baseOutputDir
  protected final String productName
  protected final ProductExtension product
  protected final File outputDir

  protected ServerConfig sconfig
  protected List<WebAppConfig> wconfigs
  protected jsonConfig
  protected String logbackConfig
  protected Map launchScripts = [:]

  ProductConfigurer(Project project, File baseOutputDir, String productName, ProductExtension product) {
    this.project = project
    this.baseOutputDir = baseOutputDir
    this.productName = productName
    this.product = product
    outputDir = new File(baseOutputDir, productName ?: product.includeVersion ? "${project.name}-${project.version}" : project.name)
  }

  void configureProduct() {

    def buildProductTask = project.task("buildProduct${productName}", group: 'gretty') { task ->

      description = "Builds gretty product${ productName ? ' ' + productName : '' }."

      dependsOn {
        resolveConfig()
        wconfigs.collectMany { wconfig ->
          def result = []
          if(wconfig.projectPath) {
            def proj = project.project(wconfig.projectPath)
            result.add(proj.tasks.build)
            if(ProjectUtils.isSpringBootApp(proj, wconfig))
              result.add(proj.tasks.jar)
          }
          result
        }
      }

      inputs.property 'config', {
        resolveConfig()
        jsonConfig.toString()
      }

      inputs.property 'logbackConfig', {
        resolveConfig()
        logbackConfig
      }

      inputs.property 'launchScripts', {
        resolveConfig()
        launchScripts
      }

      inputs.property 'textFiles', {
        createTextFiles(false)
      }

      inputs.property 'realms', {
        resolveConfig()
        [ '#server': sconfig.realm ] + wconfigs.collectEntries({ [ it.contextPath, it.realm ] })
      }

      inputs.files {
        resolveConfig()
        def result = []
        for(WebAppConfig wconfig in wconfigs) {
          // projects are already set as input in dependsOn
          if(wconfig.projectPath)
            result.addAll project.project(wconfig.projectPath).configurations.runtimeClasspath.files
          else
            result.add wconfig.resourceBase
          if(wconfig.realmConfigFile)
            result.add wconfig.realmConfigFile
          if(wconfig.contextConfigFile)
            result.add wconfig.contextConfigFile
          if(wconfig.extraResourceBases) {
            def proj = wconfig.projectPath ? project.project(wconfig.projectPath) : project
            for(def resBase in wconfig.extraResourceBases)
              result.addAll proj.fileTree(resBase).files
          }
          result
        }
        result
      }

      inputs.files {
        resolveConfig()
        def result = []
        if(sconfig.realmConfigFile)
          result.add sconfig.realmConfigFile
        if(sconfig.serverConfigFile)
          result.add sconfig.serverConfigFile
        if(sconfig.logbackConfigFile)
          result.add sconfig.logbackConfigFile
        result
      }

      inputs.files project.configurations.grettyStarter

      inputs.files project.configurations.grettyProductRuntime

      inputs.files project.configurations.grettyProvidedCompile

      inputs.files {
        resolveConfig()
        getRunnerFileCollection()
      }

      inputs.property 'logback-config-template', { getLogbackConfigTemplate() }

      outputs.dir outputDir

      ext.outputDir = outputDir
      ext.baseOutputDir = baseOutputDir

      doLast {
        resolveConfig()
        writeConfigFiles()
        writeLaunchScripts()
        writeTextFiles(createTextFiles(true))
        copyWebappFiles()
        copyStarter()
        copyRunner()
      }
    }

    project.tasks.buildAllProducts.dependsOn buildProductTask

    def archiveProductTask = project.task("archiveProduct${productName}", group: 'gretty', type: Zip) {

      description = "Archives gretty product${ productName ? ' ' + productName : '' }."

      dependsOn buildProductTask

      baseName = productName ?: project.name
      version = project.version
      destinationDir = baseOutputDir

      from outputDir, { into outputDir.name }

      doLast {
        ant.checksum file: it.archivePath
      }
    }

    project.tasks.archiveAllProducts.dependsOn archiveProductTask
  }

  void copyRunner() {

    ManagedDirectory dir = new ManagedDirectory(new File(outputDir, 'runner'))

    for(File file in getRunnerFileCollection().files)
      dir.add(file)

    File logbackConfigFile = new File(project.buildDir, 'runner/logback.' + getLogbackConfigExtension())
    logbackConfigFile.parentFile.mkdirs()
    logbackConfigFile.text = getRunnerLogbackConfig()
    dir.add(logbackConfigFile, 'logback-config')

    dir.cleanup()
  }

  void copyStarter() {

    ManagedDirectory dir = new ManagedDirectory(new File(outputDir, 'starter'))

    for(File file in project.configurations.grettyStarter.files)
      dir.add(file)

    File logbackConfigFile = new File(project.buildDir, 'starter/logback.groovy')
    logbackConfigFile.parentFile.mkdirs()
    logbackConfigFile.text = getStarterLogbackConfig()
    dir.add(logbackConfigFile, 'logback-config')

    dir.cleanup()
  }

  void copyWebappFiles() {

    ManagedDirectory webappsDir = new ManagedDirectory(new File(outputDir, 'webapps'))

    for(WebAppConfig wconfig in wconfigs) {
      String appDir = ProjectUtils.getWebAppDestinationDirName(project, wconfig)
      if(ProjectUtils.isSpringBootApp(project, wconfig)) {
        def files
        if(wconfig.projectPath) {
          def proj = project.project(wconfig.projectPath)
          for(File webappDir in ProjectUtils.getWebAppDirs(proj))
            for(File f in (webappDir.listFiles() ?: []))
              webappsDir.add(f, appDir)
          def resolvedClassPath = new LinkedHashSet<URL>()
          resolvedClassPath.addAll(ProjectUtils.getClassPathJars(proj, 'grettyProductRuntime'))
          resolvedClassPath.addAll(ProjectUtils.resolveClassPath(proj, wconfig.beforeClassPath))
          resolvedClassPath.addAll(ProjectUtils.resolveClassPath(proj, wconfig.classPath))
          files = resolvedClassPath.collect { new File(it.path) }
          files -= getVisibleRunnerFileCollection().files
        } else {
          def file = wconfig.resourceBase
          if(!(file instanceof File))
            file = new File(file.toString())
          files = [ file ]
        }
        for(File file in files) {
          if(file.isDirectory())
            for(File f in (file.listFiles() ?: []))
              webappsDir.add(f, appDir + '/WEB-INF/classes')
          else
            webappsDir.add(file, appDir + '/WEB-INF/lib')
        }
      } else {
        def file = wconfig.resourceBase
        if(!(file instanceof File))
          file = new File(file.toString())
        webappsDir.add(file)
      }
    }

    webappsDir.cleanup()

    if(wconfigs.find { it.extraResourceBases }) {
      ManagedDirectory extraResourcesDir = new ManagedDirectory(new File(outputDir, 'extraResources'))
      for(WebAppConfig wconfig in wconfigs) {
        String appDir = ProjectUtils.getWebAppDestinationDirName(project, wconfig)
        for(def resBase in wconfig.extraResourceBases)
          extraResourcesDir.add(resBase, appDir)
      }
      extraResourcesDir.cleanup()
    }
  }

  protected void createLaunchScripts() {

    String shellResolveDir = '#!/bin/bash\n' +
      'SOURCE="${BASH_SOURCE[0]}"\n' +
      'while [ -h "$SOURCE" ]; do # resolve $SOURCE until the file is no longer a symlink\n' +
      'DIR="$( cd -P "$( dirname "$SOURCE" )" && pwd )"\n' +
      'SOURCE="$(readlink "$SOURCE")"\n' +
      '[[ $SOURCE != /* ]] && SOURCE="$DIR/$SOURCE"\n' +
      'done\n' +
      'DIR="$( cd -P "$( dirname "$SOURCE" )" && pwd )"\n'

    for(String cmd in ['run', 'start', 'stop', 'restart']) {
      launchScripts[cmd + '.sh'] = shellResolveDir + 'java -Dfile.encoding=UTF8 -cp \"${DIR}/starter/*:${DIR}/starter/logback-config\" ' + mainClass + ' $@ ' + cmd
      launchScripts[cmd + '.bat'] = '@java.exe -Dfile.encoding=UTF8 -cp \"%~dp0\\starter\\*;%~dp0\\starter\\logback-config\" ' + mainClass + ' %* ' + cmd
    }
  }

  protected Map createTextFiles(boolean addTimeStamp) {
    String text = """Product: ${productName ?: project.name}
Version: ${project.version}"""
    if(addTimeStamp)
      text += "\nCreated: ${new Date()}"
    ['VERSION.txt': text]
  }

  private String getLogbackConfigExtension() {
    if(sconfig.logbackConfigFile) {
      def logbackConfigFile = sconfig.logbackConfigFile
      if(!(logbackConfigFile instanceof File))
        logbackConfigFile = new File(logbackConfigFile.toString())
      int extPos = logbackConfigFile.name.lastIndexOf('.')
      return logbackConfigFile.name.substring(extPos + 1)
    }
    'groovy'
  }

  private String getLogbackConfigTemplate() {
    ProductConfigurer.class.classLoader.getResourceAsStream('logback-config-template/logback.groovy').withStream {
      it.text
    }
  }

  protected FileCollection getRunnerFileCollection() {
    def servletContainerConfig = ServletContainerConfig.getConfig(sconfig.servletContainer)
    project.configurations.grettyNoSpringBoot + project.configurations[servletContainerConfig.servletContainerRunnerConfig]
  }

  private String getRunnerLogbackConfig() {

    if(sconfig.logbackConfigFile) {
      def logbackConfigFile = sconfig.logbackConfigFile
      if(!(logbackConfigFile instanceof File))
        logbackConfigFile = new File(logbackConfigFile.toString())
      return logbackConfigFile.text
    }

    String logDir = sconfig.logDir
    if(!logDir || logDir == "${System.getProperty('user.home')}/logs")
      logDir = '${System.getProperty(\'user.home\')}/logs'

    def logFileName = (sconfig.logFileName ?: productName ?: project.name)

    def loggingLevel = sconfig.loggingLevel ?: 'INFO'

    new groovy.text.SimpleTemplateEngine().createTemplate(getLogbackConfigTemplate()).make([
            logDir: logDir,
            logFileName: logFileName,
            loggingLevel: loggingLevel,
            consoleLogEnabled: sconfig.consoleLogEnabled,
            fileLogEnabled: sconfig.fileLogEnabled
    ]).toString()
  }

  private String getStarterLogbackConfig() {
    String logDir = sconfig.logDir
    if(!logDir || logDir == "${System.getProperty('user.home')}/logs")
      logDir = '${System.getProperty(\'user.home\')}/logs'

    def logFileName = (sconfig.logFileName ?: productName ?: project.name) + '-starter'

    def loggingLevel = sconfig.loggingLevel ?: 'INFO'

    new groovy.text.SimpleTemplateEngine().createTemplate(getLogbackConfigTemplate()).make([
            logDir: logDir,
            logFileName: logFileName,
            loggingLevel: loggingLevel,
            consoleLogEnabled: sconfig.consoleLogEnabled,
            fileLogEnabled: sconfig.fileLogEnabled
    ]).toString()
  }

  protected FileCollection getVisibleRunnerFileCollection() {
    def files = getRunnerFileCollection()
    files = files.filter { f ->
      !f.name.startsWith('slf4j') && !f.name.startsWith('logback') && !f.name.startsWith('groovy')
    }
  }

  protected resolveConfig() {
    if(sconfig != null)
      return
    FarmConfigurer configurer = new FarmConfigurer(project)
    FarmExtension productFarm = new FarmExtension(project)
    configurer.configureFarm(productFarm,
      new FarmExtension(project, serverConfig: product.serverConfig, webAppRefs: product.webAppRefs),
      configurer.findProjectFarm(productName)
    )
    sconfig = productFarm.serverConfig
    wconfigs = []
    // we don't need to pass inplaceMode here cuz inplace=false anyway
    configurer.resolveWebAppRefs(productName, productFarm.webAppRefs, wconfigs, false)
    for(WebAppConfig wconfig in wconfigs)
      ProjectUtils.prepareToRun(project, wconfig)
    CertificateGenerator.maybeGenerate(project, sconfig)
    jsonConfig = writeConfigToJson()
    createLaunchScripts()
  }

  protected void writeConfigFiles() {

    // certificate files might be deleted by clean task
    if(sconfig.sslKeyStorePath instanceof File &&
       sconfig.sslKeyStorePath.absolutePath.startsWith(new File(project.buildDir, 'ssl').absolutePath) &&
       !sconfig.sslKeyStorePath.exists()) {
      sconfig.sslKeyStorePath = null
      CertificateGenerator.maybeGenerate(project, sconfig)
      jsonConfig = writeConfigToJson()
    }

    // there are cases when springBootMainClass was accessed too early (in configuration phase),
    // so we neeed to recalculate it.
    if(wconfigs.find { ProjectUtils.isSpringBootApp(project, it) && it.springBootMainClass == null }) {
      for(WebAppConfig wconfig in wconfigs)
        ProjectUtils.prepareToRun(project, wconfig)
      jsonConfig = writeConfigToJson()
    }

    ManagedDirectory dir = new ManagedDirectory(new File(outputDir, 'conf'))

    File configFile = new File(dir.baseDir, 'server.json')
    configFile.parentFile.mkdirs()
    configFile.text = jsonConfig.toPrettyString()
    dir.registerAdded(configFile)

    if(sconfig.sslKeyStorePath && (!(sconfig.sslKeyStorePath instanceof String) || !sconfig.sslKeyStorePath.startsWith('classpath:')))
      dir.add(sconfig.sslKeyStorePath)

    if(sconfig.sslTrustStorePath && (!(sconfig.sslTrustStorePath instanceof String) || !sconfig.sslTrustStorePath.startsWith('classpath:')))
      dir.add(sconfig.sslTrustStorePath)

    if(sconfig.realmConfigFile)
      dir.add(sconfig.realmConfigFile)

    if(sconfig.serverConfigFile)
      dir.add(sconfig.serverConfigFile)

    for(WebAppConfig wconfig in wconfigs) {
      String appDir = ProjectUtils.getWebAppDestinationDirName(project, wconfig)
      if(wconfig.realmConfigFile)
        dir.add(wconfig.realmConfigFile, appDir)
      if(wconfig.contextConfigFile)
        dir.add(wconfig.contextConfigFile, appDir)
    }

    dir.cleanup()
  }

  protected writeConfigToJson() {
    def json = new JsonBuilder()
    json {
      writeConfigToJson(delegate)
    }
    json
  }

  protected void writeConfigToJson(json) {
    def self = this
    def getFileName = { file ->
      if(file == null)
        return null
      if(!(file instanceof File))
        file = new File(file.toString())
      file.name
    }
    def servletContainerConfig = ServletContainerConfig.getConfig(sconfig.servletContainer)
    json.with {
      productName self.productName ?: project.name
      servletContainer {
        id sconfig.servletContainer
        version servletContainerConfig.servletContainerVersion(project)
        description servletContainerConfig.servletContainerDescription(project)
      }
      serverConfig {
        if(sconfig.host)
          host sconfig.host
        if(sconfig.httpEnabled) {
          if(sconfig.httpPort)
            httpPort sconfig.httpPort
          if(sconfig.httpIdleTimeout)
            httpIdleTimeout sconfig.httpIdleTimeout
        } else
          httpEnabled false
        if(sconfig.httpsEnabled) {
          httpsEnabled true
          if(sconfig.httpsPort)
            httpsPort sconfig.httpsPort
          if(sconfig.httpsIdleTimeout)
            httpsIdleTimeout sconfig.httpsIdleTimeout
          // Note that sslHost is not written to config. sslHost must be only used by CertificateGenerator, which is not available in gretty products.
          if(sconfig.sslKeyStorePath) {
            if(sconfig.sslKeyStorePath instanceof File)
              sslKeyStorePath 'conf/' + sconfig.sslKeyStorePath.name
            else if(sconfig.sslKeyStorePath instanceof String)
              sslKeyStorePath sconfig.sslKeyStorePath
          }
          if(sconfig.sslKeyStorePassword)
            sslKeyStorePassword sconfig.sslKeyStorePassword
          if(sconfig.sslKeyManagerPassword)
            sslKeyManagerPassword sconfig.sslKeyManagerPassword
          if(sconfig.sslTrustStorePath) {
            if(sconfig.sslTrustStorePath instanceof File)
              sslTrustStorePath 'conf/' + sconfig.sslTrustStorePath.name
            else if(sconfig.sslTrustStorePath instanceof String)
              sslTrustStorePath sconfig.sslTrustStorePath
          }
          if(sconfig.sslTrustStorePassword)
            sslTrustStorePassword sconfig.sslTrustStorePassword
          if(sconfig.sslNeedClientAuth)
            sslNeedClientAuth sconfig.sslNeedClientAuth
        }
        if(sconfig.realm)
          realm sconfig.realm
        if(sconfig.realmConfigFile)
          realmConfigFile 'conf/' + getFileName(sconfig.realmConfigFile)
        if(sconfig.serverConfigFile)
          serverConfigFile 'conf/' + getFileName(sconfig.serverConfigFile)
        if(sconfig.secureRandom != null)
          secureRandom sconfig.secureRandom
        if(sconfig.singleSignOn != null)
          singleSignOn sconfig.singleSignOn
        if(sconfig.enableNaming != null)
          enableNaming sconfig.enableNaming        
        if(sconfig.jvmArgs)
          jvmArgs sconfig.jvmArgs
      }
      webApps wconfigs.collect { WebAppConfig wconfig ->
        { ->
          String webappDestName = ProjectUtils.getWebAppDestinationDirName(project, wconfig)
          String appConfigDir = 'conf/' + webappDestName
          contextPath wconfig.contextPath
          if(ProjectUtils.isSpringBootApp(project, wconfig))
            resourceBase 'webapps/' + ProjectUtils.getWebAppDestinationDirName(project, wconfig)
          else
            resourceBase 'webapps/' + getFileName(wconfig.resourceBase)
          if(wconfig.extraResourceBases)
            extraResourceBases wconfig.extraResourceBases.collect { 'extraResources/' + webappDestName + '/' + getFileName(it) }
          if(wconfig.initParameters)
            initParams wconfig.initParameters
          if(wconfig.realm)
            realm wconfig.realm
          if(wconfig.realmConfigFile)
            realmConfigFile appConfigDir + '/' + getFileName(wconfig.realmConfigFile)
          if(wconfig.contextConfigFile)
            contextConfigFile appConfigDir + '/' + getFileName(wconfig.contextConfigFile)
          if(ProjectUtils.isSpringBootApp(project, wconfig))
            springBoot true
          if(wconfig.springBootMainClass)
            springBootMainClass wconfig.springBootMainClass
        }
      }
    } // json
  }

  protected void writeLaunchScripts() {
    launchScripts.each { fileName, fileText ->
      File file = new File(outputDir, fileName)
      file.text = fileText
      if(fileName.endsWith('.sh'))
        file.setExecutable(true)
    }
  }

  protected void writeTextFiles(Map textFiles) {
    textFiles.each { fileName, fileText ->
      File file = new File(outputDir, fileName)
      file.text = fileText
    }
  }
}
