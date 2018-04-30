package org.akhikhl.gretty.internal.integrationTests

import org.akhikhl.gretty.AppAfterIntegrationTestTask
import org.akhikhl.gretty.AppBeforeIntegrationTestTask
import org.akhikhl.gretty.ServletContainerConfig
import org.gradle.api.JavaVersion
import org.gradle.api.Project
import org.gradle.api.tasks.Copy
import org.gradle.api.tasks.testing.Test
import org.slf4j.Logger
import org.slf4j.LoggerFactory

class IntegrationTestPlugin extends BasePlugin {

  private static final Logger log = LoggerFactory.getLogger(IntegrationTestPlugin)

  @Override
  protected void applyPlugins(Project project) {
    super.applyPlugins(project)
    project.apply plugin: 'groovy' // this is needed for spock
  }

  @Override
  protected void applyPluginsToRootProject(Project project) {
    super.applyPluginsToRootProject(project)
    project.apply plugin: de.undercouch.gradle.tasks.download.DownloadTaskPlugin
  }

  @Override
  protected void configureDependencies(Project project) {
    super.configureDependencies(project)
    project.dependencies {
      integrationTestCompile "org.codehaus.groovy:groovy-all:${project.groovy_version}"
      integrationTestCompile "org.spockframework:spock-core:${project.spock_version}"
      integrationTestCompile "org.gebish:geb-spock:${project.gebVersion}"
      integrationTestCompile "org.seleniumhq.selenium:selenium-support:${project.seleniumVersion}"
      integrationTestCompile "org.seleniumhq.selenium:selenium-firefox-driver:${project.seleniumVersion}"
      integrationTestCompile "org.gretty:gretty-spock:${project.version}"
    }
  }

  @Override
  protected void configureExtensions(Project project) {
    super.configureExtensions(project)

    project.ext.defineIntegrationTest = {

      def integrationTestTask_ = project.tasks.findByName('integrationTest')
      if(integrationTestTask_)
        return

      integrationTestTask_ = project.task('integrationTest', type: Test) {
        outputs.upToDateWhen { false }
        include '**/*IT.*', '**/*Spec.*', '**/*Test.*'
        if(project.gradle.gradleVersion.startsWith('2.') || project.gradle.gradleVersion.startsWith('3.'))
          testClassesDir = project.sourceSets.integrationTest.output.classesDir
        else
          testClassesDirs = project.sourceSets.integrationTest.output.classesDirs
        classpath = project.sourceSets.integrationTest.runtimeClasspath
      }

      integrationTestTask_
    }

    project.ext.defineIntegrationTestAllContainers = { Collection integrationTestContainers = null ->

      def integrationTestAllContainersTask = project.tasks.findByName('integrationTestAllContainers')

      if(integrationTestAllContainersTask)
        return integrationTestAllContainersTask

      integrationTestAllContainersTask = project.task('integrationTestAllContainers')

      if(!integrationTestContainers)
        // excluding jetty9.3/4 tests because of login bug
        integrationTestContainers = ServletContainerConfig.getConfigNames() - ['jetty9.3', 'jetty9.4']

      if(JavaVersion.current().isJava9Compatible()) {
        // excluding jetty7 and jetty8 under JDK9, can no longer compile JSPs to default 1.5 target,
        // see https://github.com/gretty-gradle-plugin/gretty/issues/15
        integrationTestContainers -= ['jetty7', 'jetty8']
      }

      if(project.hasProperty('testAllContainers') && project.testAllContainers) {
        integrationTestContainers.retainAll(Eval.me(project.testAllContainers))
      }

      integrationTestContainers.each { String container ->

        project.task('integrationTest_' + container, type: Test) { thisTask ->
          outputs.upToDateWhen { false }
          include '**/*IT.*', '**/*Spec.*', '**/*Test.*'
          if(project.gradle.gradleVersion.startsWith('2.') || project.gradle.gradleVersion.startsWith('3.'))
            testClassesDir = project.sourceSets.integrationTest.output.classesDir
          else
            testClassesDirs = project.sourceSets.integrationTest.output.classesDirs
          classpath = project.sourceSets.integrationTest.runtimeClasspath
        }

        integrationTestAllContainersTask.dependsOn project.tasks['integrationTest_' + container]

        project.task('beforeIntegrationTest_' + container, type: AppBeforeIntegrationTestTask) {
          servletContainer = container
          integrationTestTask 'integrationTest_' + container
        }

        project.task('afterIntegrationTest_' + container, type: AppAfterIntegrationTestTask) {
          integrationTestTask 'integrationTest_' + container
        }
      }

      for(int i = 1; i < integrationTestContainers.size(); i++) {
        String thisContainer = integrationTestContainers[i]
        String prevContainer = integrationTestContainers[i - 1]
        project.tasks['beforeIntegrationTest_' + thisContainer].mustRunAfter project.tasks['afterIntegrationTest_' + prevContainer]
      }

      integrationTestAllContainersTask
    }
  }

  @Override
  protected void configureRootProjectProperties(Project project) {
    super.configureRootProjectProperties(project)
    if(!project.hasProperty('geckoDriverArchiveFileName'))
      project.ext.geckoDriverArchiveFileName = "geckodriver-v${project.geckoDriverVersion}-${project.geckoDriverPlatform}.tar.gz"
    if(!project.hasProperty('geckoDriverDownloadUrl'))
      project.ext.geckoDriverDownloadUrl = "https://github.com/mozilla/geckodriver/releases/download/v${project.geckoDriverVersion}/${project.geckoDriverArchiveFileName}"
  }

  @Override
  protected void configureRootProjectTasksAfterEvaluate(Project project) {
    super.configureRootProjectTasksAfterEvaluate(project)

    if(!project.tasks.findByName('downloadGeckoDriver'))
      project.task('downloadGeckoDriver') {
        ext.outputDir = project.buildDir
        ext.outputFile = new File(ext.outputDir, project.geckoDriverArchiveFileName)
        inputs.property 'url', project.geckoDriverDownloadUrl
        outputs.file ext.outputFile
        doLast {
          ext.outputDir.mkdirs()
          project.download {
            src project.geckoDriverDownloadUrl
            dest ext.outputDir
            acceptAnyCertificate true
          }
        }
      }

    if(!project.tasks.findByName('unpackGeckoDriver'))
      project.task('unpackGeckoDriver', type: Copy) {
        ext.outputDir = project.buildDir
        ext.outputFile = new File(ext.outputDir, 'geckodriver')
        dependsOn project.tasks.downloadGeckoDriver
        from project.tarTree(project.resources.gzip(project.tasks.downloadGeckoDriver.ext.outputFile))
        into ext.outputDir
      }
  }

  @Override
  protected void configureSourceSets(Project project) {
    super.configureSourceSets(project)
    project.sourceSets {
      integrationTest {
        java {
          srcDir 'src/integrationTest/java'
        }
        groovy {
          srcDir 'src/integrationTest/groovy'
        }
        resources {
          srcDir 'src/integrationTest/resources'
        }
        runtimeClasspath += project.rootProject.files('config/gebConfig')
      }
    }
  }

  @Override
  protected void configureTasks(Project project) {
    super.configureTasks(project)
    project.task('testAll') {
      dependsOn project.tasks.test
    }
  }

  @Override
  protected void configureTasksAfterEvaluate(Project project) {
    super.configureTasksAfterEvaluate(project)

    project.tasks.withType(AppBeforeIntegrationTestTask) {
      dependsOn project.tasks.test
      dependsOn project.rootProject.tasks.unpackGeckoDriver
    }

    project.tasks.withType(Test) { task ->
      if(task.name != 'test')
        task.mustRunAfter project.tasks.test
      dependsOn project.rootProject.tasks.unpackGeckoDriver
      doFirst {
        systemProperty 'webdriver.gecko.driver', project.rootProject.tasks.unpackGeckoDriver.ext.outputFile.absolutePath
        systemProperty 'geb.build.reportsDir', project.reporting.file(task.name)
        systemProperty 'firefox.profile.path', project.rootProject.file('config/firefox-profile').absolutePath
      }
    }

    String sslKeyStorePassword_
    String sslKeyManagerPassword_
    new Properties().with { prop ->
      project.rootProject.file('config/self-signed-certificate/properties').withInputStream { stm ->
        prop.load(stm)
      }
      sslKeyStorePassword_ = prop.getProperty('sslKeyStorePassword')
      sslKeyManagerPassword_ = prop.getProperty('sslKeyManagerPassword')
    }

    project.gretty {
      sslKeyStorePath project.rootProject.file('config/self-signed-certificate/keystore').absolutePath
      sslKeyStorePassword sslKeyStorePassword_
      sslKeyManagerPassword sslKeyManagerPassword_
    }

    if(project.rootProject != project)
      project.rootProject.tasks.testAll.dependsOn project.tasks.testAll
  }
}
