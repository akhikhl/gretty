package org.akhikhl.gretty

import org.gradle.api.model.ReplacedBy
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity

interface WebAppConfigWithInputs {

    @Input
    @Optional
    Object getContextPath()

    @Input
    @Optional
    Object getInitParameters()

    @Internal
    Object getRealm()

    @Internal
    Object getRealmConfigFile()

    @InputFile
    @PathSensitive(PathSensitivity.NONE)
    @Optional
    Object getContextConfigFile()

    @ReplacedBy("contextConfigFile")
    Object getJettyEnvXmlFile()

    @Internal
    Object getScanDirs()

    @Input
    @Optional
    Boolean getScanDependencies()

    @Input
    @Optional
    Object getFastReload()

    @Input
    @Optional
    Boolean getRecompileOnSourceChange()

    @Input
    @Optional
    Boolean getReloadOnClassChange()

    @Input
    @Optional
    Boolean getReloadOnConfigChange()

    @Input
    @Optional
    Boolean getReloadOnLibChange()

    @Input
    @Optional
    Object getResourceBase()

    @Input
    @Optional
    List getExtraResourceBases()

    @Input
    @Optional
    Set<String> getBeforeClassPath()

    @Input
    @Optional
    Set<String> getClassPath()

    @Input
    @Optional
    String getWebInfIncludeJarPattern()

    @Input
    @Optional
    String getProjectPath()

    @Input
    @Optional
    Boolean getInplace()

    @Input
    @Optional
    String getInplaceMode()

    @Input
    @Optional
    String getWebXml()

    @Input
    @Optional
    Boolean getSpringBoot()

    @Input
    @Optional
    String getSpringBootMainClass()
}
