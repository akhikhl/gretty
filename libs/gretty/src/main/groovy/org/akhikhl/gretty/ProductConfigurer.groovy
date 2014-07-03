/*
 * gretty
 *
 * Copyright 2013  Andrey Hihlovskiy.
 *
 * See the file "license.txt" for copying and usage permission.
 */
package org.akhikhl.gretty

import groovy.json.JsonBuilder
import org.apache.commons.io.FileUtils
import org.gradle.api.Project

/**
 *
 * @author akhikhl
 */
class ProductConfigurer {

  protected static final String mainClass = 'org.akhikhl.gretty.GrettyStarter'

  protected final Project project
  protected final File baseOutputDir
  protected final String productName
  protected final ProductExtension product
  protected final File outputDir

  protected ServerConfig sconfig
  protected List<WebAppConfig> wconfigs
  protected String jsonConfig
  protected String logbackConfig
  protected Map launchScripts = [:]

  ProductConfigurer(Project project, File baseOutputDir, String productName, ProductExtension product) {
    this.project = project
    this.baseOutputDir = baseOutputDir
    this.productName = productName
    this.product = product
    outputDir = new File(baseOutputDir, productName ?: project.name)
  }

  void configureProduct() {

    def buildProductTask = project.task("buildProduct${productName}", group: 'gretty') {

      dependsOn {
        resolveConfig()
        wconfigs.findResults {
          it.projectPath ? project.project(it.projectPath).tasks.build : null
        }
      }

      inputs.property 'config', {
        resolveConfig()
        jsonConfig
      }

      inputs.property 'logbackConfig', {
        resolveConfig()
        logbackConfig
      }

      inputs.property 'launchScripts', {
        resolveConfig()
        launchScripts
      }

      inputs.files {
        resolveConfig()
        wconfigs.findResults {
          it.projectPath ? null : it.warResourceBase
        }
      }

      inputs.files {
        resolveConfig()
        sconfig.logbackConfigFile ? [ sconfig.logbackConfigFile ] : []
      }

      inputs.files project.configurations.grettyStarter

      inputs.files {
        resolveConfig()
        project.configurations[ServletContainerConfig.getConfig(sconfig.servletContainer).servletContainerRunnerConfig]
      }

      outputs.dir outputDir

      doLast {
        resolveConfig()
        writeConfig()
        writeLogbackConfig()
        writeLaunchScripts()
        copyWebappFiles()
        copyStarter()
        copyRunner()
      }
    }

    project.tasks.buildAllProducts.dependsOn buildProductTask

    def archiveProductTask = project.task("archiveProduct${productName}", group: 'gretty') {

      dependsOn buildProductTask

      doLast {
        println "archiving product $productName"
      }
    }

    project.tasks.archiveAllProducts.dependsOn archiveProductTask
  }

  void copyStarter() {
    File destDir = new File(outputDir, 'starter')
    destDir.mkdirs()
    for(File file in project.configurations.grettyStarter.files)
      FileUtils.copyFileToDirectory(file, destDir)
  }

  void copyRunner() {
    File destDir = new File(outputDir, 'runner')
    destDir.mkdirs()
    Set destFiles = new HashSet()
    for(File file in project.configurations[ServletContainerConfig.getConfig(sconfig.servletContainer).servletContainerRunnerConfig].files) {
      File destFile = new File(destDir, file.name)
      FileUtils.copyFile(file, destFile)
      destFiles.add(destFile)
    }
    for(File f in destDir.listFiles())
      if(!destFiles.contains(f)) {
        if(f.isDirectory())
          f.deleteDir()
        else
          f.delete()
      }
  }

  void copyWebappFiles() {
    File destDir = new File(outputDir, 'webapps')
    destDir.mkdirs()
    Set destFiles = new HashSet()
    for(File file in getWebappFiles()) {
      File destFile = new File(destDir, file.name)
      FileUtils.copyFile(file, destFile)
      destFiles.add(destFile)
    }
    for(File f in destDir.listFiles())
      if(!destFiles.contains(f)) {
        if(f.isDirectory())
          f.deleteDir()
        else
          f.delete()
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
      launchScripts[cmd + '.sh'] = shellResolveDir + 'java -Dfile.encoding=UTF8 -cp "${DIR}/conf/:${DIR}/starter/*" ' + mainClass + ' --' + cmd + ' --basedir="${DIR}" "$@"'
      launchScripts[cmd + '.bat'] = '@java.exe -Dfile.encoding=UTF8 -cp "%~dp0\\conf\\;%~dp0\\starter\\*" ' + mainClass + ' --' + cmd + ' --basedir="%~dp0" %*'
    }
  }

  Collection<File> getWebappFiles() {
    wconfigs.collect {
      def file = it.warResourceBase
      if(!(file instanceof File))
        file = new File(file.toString())
      file
    }
  }

  protected resolveConfig() {
    if(sconfig != null)
      return
    FarmConfigurer configurer = new FarmConfigurer(project)
    FarmExtension productFarm = new FarmExtension()
    configurer.configureFarm(productFarm,
      new FarmExtension(logDir: 'logs'),
      new FarmExtension(serverConfig: product.serverConfig, webAppRefs: product.webAppRefs),
      configurer.findProjectFarm(productName)
    )
    sconfig = productFarm.serverConfig
    wconfigs = []
    configurer.resolveWebAppRefs(productFarm.webAppRefs, wconfigs, false)
    for(WebAppConfig wconfig in wconfigs)
      wconfig.prepareToRun()
    jsonConfig = writeConfigToJson()
    if(!sconfig.logbackConfigFile)
      logbackConfig = LogbackUtils.generateLogbackConfig(sconfig)
    createLaunchScripts()
  }

  protected void writeConfig() {
    File configFile = new File(outputDir, 'conf/server.json')
    configFile.parentFile.mkdirs()
    configFile.text = jsonConfig
  }

  protected String writeConfigToJson() {
    def json = new JsonBuilder()
    json {
      writeConfigToJson(delegate)
    }
    json.toPrettyString()
  }

  protected void writeConfigToJson(json) {
    def self = this
    json.with {
      productName self.productName
      servetContainer {
        description ServletContainerConfig.getConfig(sconfig.servletContainer).servletContainerDescription
      }
      serverConfig {
        if(sconfig.host)
          host sconfig.host
        if(sconfig.httpEnabled) {
          httpPort sconfig.httpPort
          if(sconfig.httpIdleTimeout)
            httpIdleTimeout sconfig.httpIdleTimeout
        }
        if(sconfig.httpsEnabled) {
          httpsPort sconfig.httpsPort
          if(sconfig.httpsIdleTimeout)
            httpsIdleTimeout sconfig.httpsIdleTimeout
          if(sconfig.sslKeyStorePath)
            sslKeyStorePath sconfig.sslKeyStorePath.absolutePath
          if(sconfig.sslKeyStorePassword)
            sslKeyStorePassword sconfig.sslKeyStorePassword
          if(sconfig.sslKeyManagerPassword)
            sslKeyManagerPassword sconfig.sslKeyManagerPassword
          if(sconfig.sslTrustStorePath)
            sslTrustStorePath sconfig.sslTrustStorePath.absolutePath
          if(sconfig.sslTrustStorePassword)
            sslTrustStorePassword sconfig.sslTrustStorePassword
        }
        if(sconfig.jettyXmlFile)
          jettyXmlFile sconfig.jettyXmlFile.absolutePath
        logbackConfigFile 'conf/' + (sconfig.logbackConfigFile ? sconfig.logbackConfigFile.name : 'logback.groovy')
        if(sconfig.secureRandom != null)
          secureRandom sconfig.secureRandom
      }
      webApps wconfigs.collect { WebAppConfig wconfig ->
        { ->
          contextPath wconfig.contextPath
          def warFile = wconfig.warResourceBase
          if(!(warFile instanceof File))
            warFile = new File(warFile.toString())
          warResourceBase "webapps/${warFile.name}"
          if(wconfig.initParameters)
            initParams wconfig.initParameters
          if(wconfig.realm)
            realm wconfig.realm
          if(wconfig.realmConfigFile)
            realmConfigFile wconfig.realmConfigFile.absolutePath
          if(wconfig.jettyEnvXmlFile)
            jettyEnvXmlFile wconfig.jettyEnvXmlFile.absolutePath
          if(wconfig.springBootSources)
            springBootSources wconfig.springBootSources
        }
      }
    }
  }

  protected void writeLaunchScripts() {
    launchScripts.each { scriptName, scriptText ->
      File launchScriptFile = new File(outputDir, scriptName)
      launchScriptFile.text = scriptText
      if(scriptName.endsWith('.sh'))
        launchScriptFile.setExecutable(true)
    }
  }

  protected void writeLogbackConfig() {
    // sconfig.logbackConfigFile, logbackConfig, outputDir
    if(sconfig.logbackConfigFile)
      FileUtils.copyFile(sconfig.logbackConfigFile, new File(outputDir, "conf/${logbackConfigFile.name}"))
    else {
      File logbackConfigFile = new File(outputDir, 'conf/logback.groovy')
      logbackConfigFile.parentFile.mkdirs()
      logbackConfigFile.text = logbackConfig
    }
  }
}
