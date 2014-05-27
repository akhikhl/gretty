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
import org.slf4j.Logger
import org.slf4j.LoggerFactory

/**
 *
 * @author akhikhl
 */
final class Runner {

  protected static final Logger log = LoggerFactory.getLogger(Runner)

  protected final StartBaseTask startTask
  protected final Project project
  protected ServerConfig sconfig
  protected Iterable<WebAppConfig> webAppConfigs
  protected final ExecutorService executorService

  static {
    Security.addProvider(new BouncyCastleProvider());
  }

  Runner(StartBaseTask startTask) {
    this.startTask = startTask
    project = startTask.project
    RunConfig runConfig = startTask.getRunConfig()
    sconfig = runConfig.getServerConfig()
    webAppConfigs = runConfig.getWebAppConfigs()
    executorService = Executors.newSingleThreadExecutor()
  }

  private void generateAndUseSelfSignedCertificate() {
    File dir = new File(project.buildDir, 'selfSignedCertificate')
    File keystoreFile = new File(dir, 'keystore')
    File certFile = new File(dir, 'cert')
    File propertiesFile = new File(dir, 'properties')
    String sslKeyStorePassword
    String sslKeyManagerPassword
    if(!keystoreFile.exists() || !certFile.exists() || !propertiesFile.exists()) {
      dir.mkdirs()
      def keyPairGenerator = KeyPairGenerator.getInstance('RSA')
      keyPairGenerator.initialize(1024)
      def KPair = keyPairGenerator.generateKeyPair()
      def certGen = new X509V3CertificateGenerator()
      certGen.setSerialNumber(BigInteger.valueOf(new SecureRandom().nextInt(Integer.MAX_VALUE)))
      certGen.setIssuerDN(new X509Principal("CN=${sconfig.host}, OU=None, O=None L=None, C=None"))
      certGen.setNotBefore(new Date(System.currentTimeMillis() - 1000L * 60 * 60 * 24 * 30))
      certGen.setNotAfter(new Date(System.currentTimeMillis() + (1000L * 60 * 60 * 24 * 365*10)))
      certGen.setSubjectDN(new X509Principal("CN=${sconfig.host}, OU=None, O=None L=None, C=None"))
      certGen.setPublicKey(KPair.getPublic())
      certGen.setSignatureAlgorithm('MD5WithRSAEncryption')
      def PKCertificate = certGen.generateX509Certificate(KPair.getPrivate())
      certFile.withOutputStream { stm ->
        stm.write(PKCertificate.getEncoded())
      }
      def ks = KeyStore.getInstance('JKS')
      sslKeyStorePassword = RandomStringUtils.randomAlphanumeric(128)
      sslKeyManagerPassword = RandomStringUtils.randomAlphanumeric(128)
      ks.load(null, sslKeyStorePassword.toCharArray());
      ks.setKeyEntry('jetty', KPair.getPrivate(), sslKeyManagerPassword.toCharArray(), [ PKCertificate ] as Certificate[]);
      new File(dir, 'keystore').withOutputStream { stm ->
        ks.store(stm, sslKeyStorePassword.toCharArray());
      }
      new Properties().with { prop ->
        prop.setProperty('sslKeyStorePassword', sslKeyStorePassword)
        prop.setProperty('sslKeyManagerPassword', sslKeyManagerPassword)
        propertiesFile.withOutputStream { stm ->
          prop.store(stm, null)
        }
      }
    } else {
      new Properties().with { prop ->
        propertiesFile.withInputStream { stm ->
          prop.load(stm)
        }
        sslKeyStorePassword = prop.getProperty('sslKeyStorePassword')
        sslKeyManagerPassword = prop.getProperty('sslKeyManagerPassword')
      }
    }
    sconfig.sslKeyStorePath = keystoreFile.absolutePath
    sconfig.sslKeyStorePassword = sslKeyStorePassword
    sconfig.sslKeyManagerPassword = sslKeyManagerPassword
  }

  private getCommandLineJson() {
    def json = new JsonBuilder()
    json {
      servicePort sconfig.servicePort
      statusPort sconfig.statusPort
    }
    json
  }

  private getRunConfigJson() {
    def json = new JsonBuilder()
    json {
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
          sslKeyStorePath sconfig.sslKeyStorePath
        if(sconfig.sslKeyStorePassword)
          sslKeyStorePassword sconfig.sslKeyStorePassword
        if(sconfig.sslKeyManagerPassword)
          sslKeyManagerPassword sconfig.sslKeyManagerPassword
        if(sconfig.sslTrustStorePath)
          sslTrustStorePath sconfig.sslTrustStorePath
        if(sconfig.sslTrustStorePassword)
          sslTrustStorePassword sconfig.sslTrustStorePassword
      }
      if(sconfig.jettyXmlFile)
        jettyXml sconfig.jettyXmlFile.absolutePath
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
      webApps webAppConfigs.collect { WebAppConfig webAppConfig ->
        { ->
          inplace webAppConfig.inplace
          if(webAppConfig.classPath)
            webappClassPath webAppConfig.classPath
          contextPath webAppConfig.contextPath
          resourceBase (webAppConfig.inplace ? webAppConfig.inplaceResourceBase : webAppConfig.warResourceBase ?: webAppConfig.warResourceBase.toString() ?: '')
          if(webAppConfig.initParameters)
            initParams webAppConfig.initParameters
          if(webAppConfig.realm && webAppConfig.realmConfigFile) {
            realm webAppConfig.realm
            realmConfigFile webAppConfig.realmConfigFile.absolutePath
          }
          if(webAppConfig.jettyEnvXmlFile)
            jettyEnvXml webAppConfig.jettyEnvXmlFile.absolutePath
        }
      }
    }
    json
  }

  void run() {
    for(WebAppConfig webAppConfig in webAppConfigs)
      webAppConfig.prepareToRun()

    if(sconfig.httpsEnabled && !sconfig.sslKeyStorePath)
      generateAndUseSelfSignedCertificate()

    Future futureStatus = executorService.submit({ ServiceProtocol.readMessage(sconfig.statusPort) } as Callable)
    def runThread = Thread.start {
      runJetty()
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
    log.warn 'Jetty server {} started.', project.ext.jettyVersion
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

    if(startTask.getIntegrationTest())
      project.ext.grettyRunnerThread = runThread
    else {
      if(startTask.interactive) {
        System.out.println 'Press any key to stop the jetty server.'
        System.in.read()
        log.debug 'Sending command: {}', 'stop'
        ServiceProtocol.send(sconfig.servicePort, 'stop')
      } else
        System.out.println "Run 'gradle ${startTask.getStopTaskName()}' to stop the jetty server."
      runThread.join()
      log.warn 'Jetty server stopped.'
    }
  }

  protected void runJetty() {

    sconfig.onStart*.call()

    def cmdLineJson = getCommandLineJson()
    log.debug 'Command-line json: {}', cmdLineJson.toPrettyString()
    cmdLineJson = cmdLineJson.toString()

    // we are going to pass json as argument to java process.
    // under windows we must escape double quotes in process parameters.
    if(System.getProperty("os.name") =~ /(?i).*windows.*/)
      cmdLineJson = cmdLineJson.replace('"', '\\"')

    ScannerManagerBase scanman = project.ext.scannerManagerFactory.createScannerManager()
    scanman.startScanner(project, sconfig, webAppConfigs)
    Runner self = this
    try {
      project.javaexec { spec ->
        spec.classpath = project.configurations.gretty
        spec.main = 'org.akhikhl.gretty.Runner'
        spec.args = [ cmdLineJson ]
        spec.debug = startTask.debug
        spec.jvmArgs sconfig.jvmArgs
        if(startTask.jacoco) {
          String jarg = startTask.jacoco.getAsJvmArg()
          log.debug 'jacoco jvmArgs: {}', jarg
          spec.jvmArgs jarg
        }
      }
    } finally {
      scanman.stopScanner()
    }

    sconfig.onStop*.call()
  }
}
