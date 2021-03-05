package org.akhikhl.gretty

import org.gradle.api.model.ReplacedBy
import org.gradle.api.tasks.Console
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity

interface ServerConfigWithInputs {
    @Optional
    @Input
    List<String> getJvmArgs()

    @Optional
    @Input
    Map<String, String> getSystemProperties()

    @Optional
    @Input
    String getServletContainer()

    @Optional
    @Input
    Boolean getManagedClassReload()

    @Optional
    @Input
    String getHost()

    @Optional
    @Input
    Boolean getHttpEnabled()

    @Optional
    @Input
    Integer getHttpPort()

    @Optional
    @Input
    Integer getHttpIdleTimeout()

    @Optional
    @Input
    Boolean getHttpsEnabled()

    @Optional
    @Input
    Integer getHttpsPort()

    @Optional
    @Input
    Integer getHttpsIdleTimeout()

    @Optional
    @Input
    String getSslHost()

    @Optional
    @InputFile
    @PathSensitive(PathSensitivity.NONE)
    Object getSslKeyStorePath()

    @Internal
    String getSslKeyStorePassword()

    @Internal
    String getSslKeyManagerPassword()

    @Optional
    @InputFile
    @PathSensitive(PathSensitivity.NONE)
    Object getSslTrustStorePath()

    @Internal
    String getSslTrustStorePassword()

    @Input
    boolean getSslNeedClientAuth()

    @Internal
    Object getRealm()

    @Internal
    Object getRealmConfigFile()

    @Internal
    Object getServerConfigFile()

    @Optional @Input
    String getInteractiveMode()

    @Optional @Input
    Integer getScanInterval()

    @Optional
    @InputFile
    @PathSensitive(PathSensitivity.NONE)
    Object getLogbackConfigFile()

    @Console
    String getLoggingLevel()

    @Console
    Boolean getConsoleLogEnabled()

    @Console
    Boolean getFileLogEnabled()

    @Internal
    Object getLogFileName()

    @Internal
    Object getLogDir()

    @Internal
    List<Closure> getOnStart()

    @Internal
    List<Closure> getOnStop()

    @Internal
    List<Closure> getOnScan()

    @Internal
    List<Closure> getOnScanFilesChanged()

    @Optional @Input
    Boolean getSecureRandom()

    @Optional @Input
    String getSpringBootVersion()

    @Optional @Input
    String getSpringLoadedVersion()

    @Optional @Input
    String getSpringVersion()

    @Optional @Input
    String getLogbackVersion()

    @Optional @Input
    Boolean getSingleSignOn()

    @Optional @Input
    Boolean getEnableNaming()

    @Optional @Input
    String getRedeployMode()

    @Optional @Input
    String getScanner()

    @Optional @Input
    String getPortPropertiesFileName()

    @Optional @Input
    Boolean getLiveReloadEnabled()

    @ReplacedBy("serverConfigFile")
    Object getJettyXmlFile()

    @Internal
    int getRandomFreePort()
}
