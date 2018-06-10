package org.akhikhl.gretty.scanner

import org.akhikhl.gretty.*
import org.gradle.api.GradleException
import org.gradle.api.Project
import org.gradle.tooling.GradleConnector
import org.gradle.tooling.ProjectConnection
import groovy.transform.CompileStatic
import groovy.transform.TypeCheckingMode
import org.slf4j.Logger
import org.slf4j.LoggerFactory
/**
 * @author timur.shakurov@dz.ru
 */
@CompileStatic(TypeCheckingMode.SKIP)
abstract class BaseScannerManager implements ScannerManager {
    private static final Logger log = LoggerFactory.getLogger(BaseScannerManager.class)

    protected static final webConfigFiles = [
      'web.xml', 'web-fragment.xml',
      'jetty.xml', 'jetty7.xml', 'jetty8.xml', 'jetty9.xml',
      'jetty-env.xml', 'jetty7-env.xml', 'jetty8-env.xml', 'jetty9-env.xml',
      'tomcat-users.xml'
    ] as HashSet

    protected Project project
    protected ServerConfig sconfig
    protected List<WebAppConfig> webapps
    protected boolean managedClassReload

    protected Map fastReloadMap

    protected Closure onBeforeFastReload
    protected Closure onFastReload
    //
    protected Closure onBeforeRestart
    protected Closure onRestart
    //
    protected Closure onBeforeReload
    protected Closure onReload

    @Override
    void registerFastReloadCallbacks(Closure before, Closure after) {
        onBeforeFastReload = before
        onFastReload = after
    }

    @Override
    void registerRestartCallbacks(Closure before, Closure after) {
        onBeforeRestart = before
        onRestart = after
    }

    @Override
    void registerReloadCallbacks(Closure before, Closure after) {
        onBeforeReload = before
        onReload = after
    }

    BaseScannerManager(Project project, ServerConfig sconfig, List<WebAppConfig> webapps, boolean managedClassReload) {
        this.project = project
        this.sconfig = sconfig
        this.webapps = webapps
        this.managedClassReload = managedClassReload
    }

    protected static void collectScanDirs(Collection<File> scanDirs, Boolean scanDependencies, Project proj) {
      scanDirs.add(ProjectUtils.getWebAppDir(proj))
      scanDirs.addAll(proj.sourceSets.main.allSource.srcDirs)
      scanDirs.addAll(proj.sourceSets.main.runtimeClasspath.files)
      if(scanDependencies) {
        collectDependenciesSourceSets(scanDirs, proj)
      }
      for(def overlay in proj.gretty.overlays)
        // TODO: Do we need to scan overlay dependencies?
        collectScanDirs(scanDirs, false, proj.project(overlay))
    }

    protected static void collectDependenciesSourceSets(Collection<File> scanDirs, Project p) {
        List<Project> dependencyProjects = ProjectUtils.getDependencyProjects(p, 'implementation')
        for(Project project: dependencyProjects) {
            // adding sourceSets of dependecy project
            scanDirs.addAll(project.sourceSets.main.allSource.srcDirs)
            // TODO: add runtime classpath?
            // repeat for dependency's dependencies
            collectDependenciesSourceSets(scanDirs, project)
        }
    }

    protected void configureFastReload(List<File> scanDirs) {
        fastReloadMap = [:]
        for (WebAppConfig webapp in webapps) {
            if (webapp.inplace && webapp.projectPath) {
                def proj = project.project(webapp.projectPath)
                List<FileReloadSpec> fastReloadSpecs = ProjectReloadUtils.getFastReloadSpecs(proj, webapp.fastReload)
                fastReloadMap[webapp.projectPath] = fastReloadSpecs
                for (FileReloadSpec spec in fastReloadSpecs) {
                    if (!scanDirs.find { File scanDir -> spec.baseDir.absolutePath.startsWith(scanDir.absolutePath) })
                        scanDirs.add(spec.baseDir)
                }
            }
        }
    }

    protected List<File> getEffectiveScanDirs() {
        Set<File> scanDirs = new LinkedHashSet()
        for(WebAppConfig webapp in webapps) {
            if(webapp.projectPath) {
                def proj = project.project(webapp.projectPath)
                collectScanDirs(scanDirs, webapp.scanDependencies, proj)
                for(def dir in webapp.scanDirs) {
                    if(!(dir instanceof File))
                        dir = proj.file(dir.toString())
                    scanDirs.add(dir)
                }
            }
        }
        return scanDirs as List
    }

    protected List<File> getProjectScanDirs(WebAppConfig webapp) {
        Set<File> scanDirs = new LinkedHashSet<>()
        if(webapp.projectPath) {
            def proj = project.project(webapp.projectPath)
            collectScanDirs(scanDirs, webapp.scanDependencies, proj)
            for(def dir in webapp.scanDirs) {
                if(!(dir instanceof File))
                    dir = proj.file(dir.toString())
                scanDirs.add(dir)
            }
        }
        return scanDirs as List
    }

    protected static isWebConfigFile(File f) {
        webConfigFiles.contains(f.name)
    }

    protected void scanFilesChanged(Collection<String> changedFiles) {

        for(def f in changedFiles)
            log.debug 'changedFile={}', f

        sconfig.onScanFilesChanged*.call(changedFiles)

        Map<WebAppConfig> webAppProjectReloads = [:]

        def reloadProject = { String projectPath, String reloadMode ->
            if(webAppProjectReloads[projectPath] == null)
                webAppProjectReloads[projectPath] = new HashSet()
            webAppProjectReloads[projectPath] += reloadMode
        }

        Set<WebAppConfig> webAppConfigsToRestart = []
        for(String f in changedFiles) {
            if(f.endsWith('.jar')) {
                List<WebAppConfig> dependantWebAppProjects = webapps.findAll {
                    it.projectPath && project.project(it.projectPath).configurations.implementation.resolvedConfiguration.resolvedArtifacts.find {
                        it.file.absolutePath == f }
                }
                if(dependantWebAppProjects) {
                    for(WebAppConfig wconfig in dependantWebAppProjects) {
                        if(wconfig.recompileOnSourceChange) {
                            log.info 'changed file {} is dependency of {}, the latter will be recompiled', f, wconfig.projectPath
                            reloadProject(wconfig.projectPath, 'compile')
                            if(managedClassReload)
                                webAppConfigsToRestart.add(wconfig)
                            // Otherwise there's no need to restart. When compilation finishes, scanner will wake up again and then restart.
                        }
                    }
                    continue
                }
            }
            webapps.findAll {
                getProjectScanDirs(it).find {
                    f.startsWith(it.absolutePath)
                }
            }.each { wconfig ->
                log.info 'changed file {} affects project {}', f, wconfig.projectPath
                def proj = project.project(wconfig.projectPath)
                if(proj.sourceSets.main.allSource.srcDirs.find { f.startsWith(it.absolutePath) }) {
                    if(wconfig.recompileOnSourceChange) {
                        reloadProject(wconfig.projectPath, 'compile')
                        // restart is done when reacting to class change, not source change
                    }
                } else if (proj.sourceSets.main.output.files.find { f.startsWith(it.absolutePath) }) {
                    if(wconfig.reloadOnClassChange) {
                        if(managedClassReload) {
                            if(wconfig.inplace)
                                log.info 'file {} is in managed output of {}, servlet-container will not be restarted', f, wconfig.projectPath
                            else {
                                log.info 'file {} is in output of {}, but it runs as WAR, servlet-container will be restarted', f, wconfig.projectPath
                                webAppConfigsToRestart.add(wconfig)
                            }
                        } else {
                            log.info 'file {} is in output of {}, servlet-container will be restarted', f, wconfig.projectPath
                            webAppConfigsToRestart.add(wconfig)
                        }
                    }
                } else if (isWebConfigFile(new File(f))) {
                    if(wconfig.reloadOnConfigChange) {
                        log.info 'file {} is configuration file, servlet-container will be restarted', f
                        reloadProject(wconfig.projectPath, 'compile')
                        webAppConfigsToRestart.add(wconfig)
                    }
                } else if(f.startsWith(new File(ProjectUtils.getWebAppDir(proj), 'WEB-INF/lib').absolutePath)) {
                    if(wconfig.reloadOnLibChange) {
                        log.info 'file {} is in WEB-INF/lib, servlet-container will be restarted', f
                        reloadProject(wconfig.projectPath, 'compile')
                        webAppConfigsToRestart.add(wconfig)
                    }
                } else if(ProjectReloadUtils.satisfiesOneOfReloadSpecs(f, fastReloadMap[proj.path])) {
                    log.info 'file {} is in fastReload directories', f
                    reloadProject(wconfig.projectPath, 'fastReload')
                } else {
                    log.info 'file {} is not in fastReload directories, switching to fullReload', f
                    reloadProject(wconfig.projectPath, 'compile')
                    webAppConfigsToRestart.add(wconfig)
                }
            }
        }

        webAppProjectReloads.each { String projectPath, Set reloadModes ->
            Project proj = project.project(projectPath)
            if(reloadModes.contains('compile')) {
                log.info 'Recompiling {}', (projectPath == ':' ? proj.name : projectPath)
                WebAppConfig wconfig = webapps.find { it.projectPath == projectPath }
                ProjectConnection connection = GradleConnector.newConnector().useInstallation(proj.gradle.gradleHomeDir).forProjectDirectory(proj.projectDir).connect()
                try {
                    connection.newBuild().forTasks(wconfig.inplace ? 'prepareInplaceWebApp' : 'prepareArchiveWebApp').run()
                } finally {
                    connection.close()
                }
            } else if(reloadModes.contains('fastReload')) {
                log.info 'Fast-reloading {}', (projectPath == ':' ? proj.name : projectPath)
                onBeforeFastReload?.call()
                WebAppConfig wconfig = webapps.find { it.projectPath == projectPath }
                // TODO: maybe we should disable fastReload at all?
                if(!(wconfig.inplace && wconfig.inplaceMode == 'hard')) {
                    ProjectUtils.prepareInplaceWebAppFolder(proj)
                }
                onFastReload?.call()
            }
        }

        if(webAppConfigsToRestart) {
            File portPropertiesFile = DefaultLauncher.getPortPropertiesFile(project, sconfig)
            if(!portPropertiesFile.exists())
                throw new GradleException("Gretty seems to be not running, cannot send command 'restart' to it.")
            Properties portProps = new Properties()
            portPropertiesFile.withReader 'UTF-8', {
                portProps.load(it)
            }
            int servicePort = portProps.servicePort as int
            if(sconfig.redeployMode == 'restart') {
                onBeforeRestart?.call()
                ServiceProtocol.send(servicePort, 'restartWithEvent')
                onRestart?.call()
            } else if(sconfig.redeployMode == 'redeploy') {
                onBeforeReload?.call()
                ServiceProtocol.send(servicePort, "redeploy ${webAppConfigsToRestart.collect {it.contextPath}.toSet().join(' ')}")
                onReload?.call()
            } else {
                throw new IllegalStateException("Unknown redeployMode: ${sconfig.redeployMode}")
            }
        }
    }

    @Override
    void stopScanner() {
        project = null
        sconfig = null
        webapps = null
        fastReloadMap = null
    }
}

