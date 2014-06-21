/*
 * gretty
 *
 * Copyright 2013  Andrey Hihlovskiy.
 *
 * See the file "license.txt" for copying and usage permission.
 */
package org.akhikhl.gretty

import groovy.json.JsonBuilder
import java.security.KeyStore
import java.security.Security
import java.security.SecureRandom
import java.security.cert.Certificate
import java.util.concurrent.Callable
import java.util.concurrent.Executors
import java.util.concurrent.ExecutorService
import java.util.concurrent.Future
import org.apache.commons.lang3.RandomStringUtils
import org.bouncycastle.jce.X509Principal
import org.bouncycastle.jce.provider.BouncyCastleProvider
import org.bouncycastle.jce.provider.asymmetric.ec.KeyPairGenerator
import org.bouncycastle.x509.X509V3CertificateGenerator
import org.gradle.api.GradleException
import org.gradle.api.Project
import org.gradle.api.file.FileCollection
import org.gradle.process.JavaExecSpec
import org.slf4j.Logger
import org.slf4j.LoggerFactory

/**
 *
 * @author akhikhl
 */
class DefaultLauncher implements Launcher {

  protected static final Logger log = LoggerFactory.getLogger(DefaultLauncher)

  protected Project project
  protected LauncherConfig config
  protected ServerConfig sconfig
  protected Iterable<WebAppConfig> webAppConfigs
  protected final ExecutorService executorService

  static {
    Security.addProvider(new BouncyCastleProvider());
  }

  DefaultLauncher(Project project, LauncherConfig config) {
    this.project = project
    this.config = config
    sconfig = config.getServerConfig()
    webAppConfigs = config.getWebAppConfigs()
    executorService = Executors.newSingleThreadExecutor()
  }

  protected void configureJavaExec(JavaExecSpec spec) {

    def cmdLineJson = getCommandLineJson()
    log.debug 'Command-line json: {}', cmdLineJson.toPrettyString()
    cmdLineJson = cmdLineJson.toString()

    // we are going to pass json as argument to java process.
    // under windows we must escape double quotes in process parameters.
    if(System.getProperty("os.name") =~ /(?i).*windows.*/)
      cmdLineJson = cmdLineJson.replace('"', '\\"')

    if(log.isDebugEnabled())
      getRunnerClassPath().each {
        log.debug 'runnerclasspath: {}', it
      }
    spec.classpath = getRunnerClassPath()

    spec.main = 'org.akhikhl.gretty.Runner'
    spec.args = [ cmdLineJson ]

    spec.debug = config.getDebug()

    log.debug 'server-config jvmArgs: {}', sconfig.jvmArgs
    spec.jvmArgs sconfig.jvmArgs

    if(config.getJacocoConfig()) {
      String jarg = config.getJacocoConfig().getAsJvmArg()
      log.debug 'jacoco jvmArgs: {}', jarg
      spec.jvmArgs jarg
    }

    if(config.getManagedClassReload()) {
      spec.jvmArgs '-javaagent:' + getSpringLoadedAgent().absolutePath, '-noverify'
      spec.systemProperty 'springloaded', 'exclusions=org.akhikhl.gretty..*'
    }

    // Speeding up tomcat startup, according to http://wiki.apache.org/tomcat/HowTo/FasterStartUp
    // ATTENTION: replacing the blocking entropy source (/dev/random) with a non-blocking one
    // actually reduces security because you are getting less-random data.
    spec.systemProperty 'java.security.egd', 'file:/dev/./urandom'
  }

  private void generateAndUseSelfSignedCertificate() {
    File dir = new File(project.buildDir, 'ssl')
    File keystoreFile = new File(dir, 'keystore')
    File certFile = new File(dir, 'cert')
    File propertiesFile = new File(dir, 'properties')
    String sslKeyStorePassword
    String sslKeyManagerPassword
    if(!keystoreFile.exists() || !certFile.exists() || !propertiesFile.exists()) {
      dir.mkdirs()
      log.warn 'Generating RSA key'
      def keyPairGenerator = KeyPairGenerator.getInstance('RSA', 'BC')
      keyPairGenerator.initialize(1024, new SecureRandom())
      def KPair = keyPairGenerator.generateKeyPair()
      log.warn 'Generating self-signed X.509 certificate'
      def certGen = new X509V3CertificateGenerator()
      certGen.setSerialNumber(BigInteger.valueOf(new SecureRandom().nextInt(Integer.MAX_VALUE)))
      String userName = System.getProperty('user.name')
      certGen.setIssuerDN(new X509Principal("CN=gretty-issuer, OU=None, O=Gretty, L=None, C=None"))
      certGen.setNotBefore(new Date(System.currentTimeMillis() - 1000L * 60 * 60 * 24 * 30))
      certGen.setNotAfter(new Date(System.currentTimeMillis() + (1000L * 60 * 60 * 24 * 365*10)))
      certGen.setSubjectDN(new X509Principal("CN=${sconfig.host}, OU=None, O=${project.name}, L=None, C=None"))
      certGen.setPublicKey(KPair.getPublic())
      certGen.setSignatureAlgorithm('SHA256WithRSA') // MD5WithRSAEncryption
      def PKCertificate = certGen.generateX509Certificate(KPair.getPrivate(), 'BC')
      log.warn 'Writing certificate to {}', certFile.absolutePath - project.projectDir.absolutePath - '/'
      certFile.withOutputStream { stm ->
        stm.write(PKCertificate.getEncoded())
      }
      def ks = KeyStore.getInstance('JKS')
      sslKeyStorePassword = RandomStringUtils.randomAlphanumeric(128)
      sslKeyManagerPassword = RandomStringUtils.randomAlphanumeric(128)
      ks.load(null, sslKeyStorePassword.toCharArray());
      ks.setKeyEntry('jetty', KPair.getPrivate(), sslKeyManagerPassword.toCharArray(), [ PKCertificate ] as Certificate[]);
      log.warn 'Writing key and certificate to {}', keystoreFile.absolutePath - project.projectDir.absolutePath - '/'
      keystoreFile.withOutputStream { stm ->
        ks.store(stm, sslKeyStorePassword.toCharArray());
      }
      log.warn 'Writing keystore passwords to {}', propertiesFile.absolutePath - project.projectDir.absolutePath - '/'
      new Properties().with { prop ->
        prop.setProperty('sslKeyStorePassword', sslKeyStorePassword)
        prop.setProperty('sslKeyManagerPassword', sslKeyManagerPassword)
        propertiesFile.withOutputStream { stm ->
          prop.store(stm, null)
        }
      }
    } else {
      log.warn 'Using RSA key and self-signed X.509 certificate from {}', keystoreFile.absolutePath - project.projectDir.absolutePath - '/'
      log.warn 'Reading keystore passwords from {}', propertiesFile.absolutePath - project.projectDir.absolutePath - '/'
      new Properties().with { prop ->
        propertiesFile.withInputStream { stm ->
          prop.load(stm)
        }
        sslKeyStorePassword = prop.getProperty('sslKeyStorePassword')
        sslKeyManagerPassword = prop.getProperty('sslKeyManagerPassword')
      }
    }
    sconfig.sslKeyStorePath = keystoreFile
    sconfig.sslKeyStorePassword = sslKeyStorePassword
    sconfig.sslKeyManagerPassword = sslKeyManagerPassword
  }

  protected getCommandLineJson() {
    def json = new JsonBuilder()
    json {
      servicePort sconfig.servicePort
      statusPort sconfig.statusPort
      serverManagerFactory getServerManagerFactory()
    }
    json
  }

  private getRunConfigJson() {
    def json = new JsonBuilder()
    json {
      writeRunConfigJson(delegate)
    }
    json
  }

  protected FileCollection getRunnerClassPath() {
    project.configurations.gretty + project.configurations[getServletContainerConfig().servletContainerRunnerConfig]
  }

  protected String getServerManagerFactory() {
    'org.akhikhl.gretty.ServerManagerFactory'
  }

  protected Map getServletContainerConfig() {
    ServletContainerConfig.getConfig(sconfig.servletContainer)
  }

  protected File getSpringLoadedAgent() {
    project.configurations.grettySpringLoaded.singleFile
  }

  final void launch() {

    for(WebAppConfig webAppConfig in webAppConfigs)
      prepareToRun(webAppConfig)

    if(sconfig.httpsEnabled) {
      if(sconfig.sslKeyStorePath)
        log.warn 'Using cryptographic key and certificate from: {}', sconfig.sslKeyStorePath
      else
        generateAndUseSelfSignedCertificate()
    }

    Future futureStatus = executorService.submit({ ServiceProtocol.readMessage(sconfig.statusPort) } as Callable)
    def runThread = Thread.start {
      launchProcess()
    }
    def status = futureStatus.get()
    log.debug 'Got init status: {}', status

    futureStatus = executorService.submit({ ServiceProtocol.readMessage(sconfig.statusPort) } as Callable)
    def runConfigJson = getRunConfigJson()
    log.debug 'Sending parameters to port {}', sconfig.servicePort
    log.debug runConfigJson.toPrettyString()
    ServiceProtocol.send(sconfig.servicePort, runConfigJson.toString())
    status = futureStatus.get()
    log.debug 'Got start status: {}', status

    System.out.println()
    log.warn '{} started.', getServletContainerConfig().fullName
    for(WebAppConfig webAppConfig in webAppConfigs) {
      String webappName
      if(webAppConfig.inplace)
        webappName = webAppConfig.projectPath
      else {
        def warFile = webAppConfig.warResourceBase
        if(!(warFile instanceof File))
          warFile = new File(warFile.toString())
        webappName = warFile.name
      }
      if(sconfig.httpEnabled && sconfig.httpsEnabled) {
        log.warn '{} runs at the addresses:', webappName
        log.warn '  http://{}:{}{}', sconfig.host, sconfig.httpPort, webAppConfig.contextPath
        log.warn '  https://{}:{}{}', sconfig.host, sconfig.httpsPort, webAppConfig.contextPath
      }
      else if(sconfig.httpEnabled)
        log.warn '{} runs at the address http://{}:{}{}', webappName, sconfig.host, sconfig.httpPort, webAppConfig.contextPath
      else if(sconfig.httpsEnabled)
        log.warn '{} runs at the address https://{}:{}{}', webappName, sconfig.host, sconfig.httpsPort, webAppConfig.contextPath
    }
    log.info 'servicePort: {}, statusPort: {}', sconfig.servicePort, sconfig.statusPort

    if(config.getIntegrationTest())
      project.ext.grettyRunnerThread = runThread
    else {
      if(config.getInteractive()) {
        System.out.println 'Press any key to stop the jetty server.'
        System.in.read()
        log.debug 'Sending command: {}', 'stop'
        ServiceProtocol.send(sconfig.servicePort, 'stop')
      } else
        System.out.println "Run 'gradle ${config.getStopTaskName()}' to stop the jetty server."
      runThread.join()
      log.warn '{} stopped.', getServletContainerConfig().fullName
    }
  }

  protected void launchProcess() {

    sconfig.onStart*.call()

    ScannerManager scanman = new ScannerManager()
    scanman.startScanner(project, sconfig, webAppConfigs, config.getManagedClassReload())
    try {
      project.javaexec this.&configureJavaExec
    } finally {
      scanman.stopScanner()
    }

    sconfig.onStop*.call()
  }

  protected Collection<URL> resolveWebAppClassPath(WebAppConfig wconfig) {
    def resolvedClassPath = new LinkedHashSet<URL>()
    if(wconfig.projectPath) {
      def proj = project.project(wconfig.projectPath)
      String runtimeConfig = ProjectUtils.isSpringBootApp(proj) ? 'springBoot' : 'runtime'
      resolvedClassPath.addAll(ProjectUtils.getClassPath(proj, wconfig.inplace, runtimeConfig))
      if(wconfig.classPath != null)
        for(def elem in wconfig.classPath) {
          while(elem instanceof Closure)
            elem = elem()
          if(elem != null) {
            if(elem instanceof File) {
              if(!elem.isAbsolute()) {
                if(proj == null)
                  elem = new File(System.getProperty('user.home'), elem.path).absolutePath
                else
                  elem = new File(proj.projectDir, elem.path)
              }
              elem = elem.toURI().toURL()
            }
            else if(elem instanceof URI)
              elem = elem.toURL()
            else if(!(elem instanceof URL)) {
              elem = elem.toString()
              if(!(elem =~ /.{2,}\:.+/)) { // no schema?
                if(!new File(elem).isAbsolute()) {
                  if(proj == null)
                    elem = new File(System.getProperty('user.home'), elem).absolutePath
                  else
                    elem = new File(proj.projectDir, elem).absolutePath
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
    }
    return resolvedClassPath
  }

  protected void prepareToRun(WebAppConfig wconfig) {
    wconfig.prepareToRun()
  }

  protected void writeLoggingConfig(json) {
    json.with {
      if(sconfig.logbackConfigFile)
        logbackConfig sconfig.logbackConfigFile.absolutePath
      else
        logging {
          loggingLevel sconfig.loggingLevel
          consoleLogEnabled sconfig.consoleLogEnabled
          fileLogEnabled sconfig.fileLogEnabled
          logFileName sconfig.logFileName
          logDir sconfig.logDir
        }
    }
  }

  protected void writeRunConfigJson(json) {
    def self = this
    json.with {
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
        jettyXml sconfig.jettyXmlFile.absolutePath
      writeLoggingConfig(json)
      webApps webAppConfigs.collect { WebAppConfig webAppConfig ->
        { ->
          inplace webAppConfig.inplace
          self.writeWebAppClassPath(delegate, webAppConfig)
          contextPath webAppConfig.contextPath
          resourceBase (webAppConfig.inplace ? webAppConfig.inplaceResourceBase : webAppConfig.warResourceBase ?: webAppConfig.warResourceBase.toString() ?: '')
          if(webAppConfig.initParameters)
            initParams webAppConfig.initParameters
          if(webAppConfig.realm)
            realm webAppConfig.realm
          if(webAppConfig.realmConfigFile)
            realmConfigFile webAppConfig.realmConfigFile.absolutePath
          if(webAppConfig.jettyEnvXmlFile)
            jettyEnvXml webAppConfig.jettyEnvXmlFile.absolutePath
          if(webAppConfig.springBootSources)
            springBootSources webAppConfig.springBootSources
        }
      }
    }
  }

  protected void writeWebAppClassPath(json, WebAppConfig webAppConfig) {
    def classPath = resolveWebAppClassPath(webAppConfig)
    if(classPath)
      json.webappClassPath classPath
  }
}
