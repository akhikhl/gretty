/*
 * Gretty
 *
 * Copyright (C) 2013-2015 Andrey Hihlovskiy and contributors.
 *
 * See the file "LICENSE" for copying and usage permission.
 * See the file "CONTRIBUTORS" for complete list of contributors.
 */
package org.akhikhl.gretty

import java.security.KeyPairGenerator
import java.security.KeyStore
import java.security.Security
import java.security.SecureRandom
import java.security.cert.Certificate
import org.apache.commons.lang3.RandomStringUtils
import org.bouncycastle.jce.X509Principal
import org.bouncycastle.jce.provider.BouncyCastleProvider
import org.bouncycastle.jcajce.provider.asymmetric.ec.KeyPairGeneratorSpi
import org.bouncycastle.x509.X509V3CertificateGenerator
import org.gradle.api.Project
import org.slf4j.Logger
import org.slf4j.LoggerFactory

/**
 *
 * @author akhikhl
 */
class CertificateGenerator {

  protected static final Logger log = LoggerFactory.getLogger(CertificateGenerator)

  protected static boolean providerAdded = false

  static void maybeGenerate(Project project, ServerConfig sconfig) {

    if(!sconfig.httpsEnabled) {
      log.debug 'https not enabled, certificate generator will be disabled'
      return
    }

    if(!providerAdded) {
      providerAdded = true
      Security.addProvider(new BouncyCastleProvider())
    }

    if(sconfig.sslKeyStorePath) {
      log.info 'Using cryptographic key and certificate from: {}', sconfig.sslKeyStorePath
      return
    }

    File dir = new File(project.buildDir, 'ssl')
    File keystoreFile = new File(dir, 'keystore')
    File certFile = new File(dir, 'cert')
    File propertiesFile = new File(dir, 'properties')
    String sslKeyStorePassword = null
    String sslKeyManagerPassword = null
    if(!keystoreFile.exists() || !certFile.exists() || !propertiesFile.exists()) {
      dir.mkdirs()
      log.info 'Generating RSA key'
      KeyPairGenerator keyPairGenerator = KeyPairGeneratorSpi.getInstance('RSA', 'BC')
      keyPairGenerator.initialize(1024, new SecureRandom())
      def KPair = keyPairGenerator.generateKeyPair()
      log.info 'Generating self-signed X.509 certificate'
      def certGen = new X509V3CertificateGenerator()
      certGen.setSerialNumber(BigInteger.valueOf(new SecureRandom().nextInt(Integer.MAX_VALUE)))
      certGen.setIssuerDN(new X509Principal("CN=gretty-issuer, OU=None, O=Gretty, L=None, C=None"))
      certGen.setNotBefore(new Date(System.currentTimeMillis() - 1000L * 60 * 60 * 24 * 30))
      certGen.setNotAfter(new Date(System.currentTimeMillis() + (1000L * 60 * 60 * 24 * 365*10)))
      String host = sconfig.sslHost ?: sconfig.host ?: 'localhost'
      certGen.setSubjectDN(new X509Principal("CN=${host}, OU=None, O=${project.name}, L=None, C=None"))
      certGen.setPublicKey(KPair.getPublic())
      certGen.setSignatureAlgorithm('SHA256WithRSA')
      def PKCertificate = certGen.generateX509Certificate(KPair.getPrivate(), 'BC')
      log.info 'Writing certificate to {}', certFile.absolutePath - project.projectDir.absolutePath - '/'
      certFile.withOutputStream { stm ->
        stm.write(PKCertificate.getEncoded())
      }
      def ks = KeyStore.getInstance('JKS')
      sslKeyStorePassword = RandomStringUtils.randomAlphanumeric(128)
      sslKeyManagerPassword = RandomStringUtils.randomAlphanumeric(128)
      ks.load(null, sslKeyStorePassword.toCharArray());
      ks.setKeyEntry('jetty', KPair.getPrivate(), sslKeyManagerPassword.toCharArray(), [ PKCertificate ] as Certificate[]);
      log.info 'Writing key and certificate to {}', keystoreFile.absolutePath - project.projectDir.absolutePath - '/'
      keystoreFile.withOutputStream { stm ->
        ks.store(stm, sslKeyStorePassword.toCharArray());
      }
      log.info 'Writing keystore passwords to {}', propertiesFile.absolutePath - project.projectDir.absolutePath - '/'
      new Properties().with { prop ->
        prop.setProperty('sslKeyStorePassword', sslKeyStorePassword)
        prop.setProperty('sslKeyManagerPassword', sslKeyManagerPassword)
        propertiesFile.withOutputStream { stm ->
          prop.store(stm, null)
        }
      }
    } else {
      log.info 'Using RSA key and self-signed X.509 certificate from {}', keystoreFile.absolutePath - project.projectDir.absolutePath - '/'
      log.info 'Reading keystore passwords from {}', propertiesFile.absolutePath - project.projectDir.absolutePath - '/'
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
}

