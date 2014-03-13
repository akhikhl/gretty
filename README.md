![logo](images/gretty_logo.png "gretty logo")

[![Build Status](https://travis-ci.org/akhikhl/gretty.png?branch=master)](https://travis-ci.org/akhikhl/gretty) [![Maintainer Status](http://stillmaintained.com/akhikhl/gretty.png)](http://stillmaintained.com/akhikhl/gretty) [![Latest Version](http://img.shields.io/badge/latest_version-0.0.13-blue.svg)](https://github.com/akhikhl/gretty/tree/v0.0.13) [![License](http://img.shields.io/badge/license-MIT-ff69b4.svg)](#copyright-and-license)

If you are new with gretty, good starting point would be learning [main features](#main-features).
If you already use gretty, it is always a good idea to look in [what's new](whatsnew.md) section.
All versions of gretty are available at jcenter and maven central under the group 'org.akhikhl.gretty'.

### Content of this document:

* [Main features](#main-features)
* [What's new](whatsnew.md)
* [Usage](#usage)
* [Switching between jetty and servlet-API versions](#switching-between-jetty-and-servlet-api-versions)
* [Supported tasks](#supported-tasks)
  * [jettyRun](#jettyrun)
  * [run](#run)
  * [jettyRunDebug](#jettyrundebug)
  * [debug](#debug)
  * [jettyRunWar](#jettyrunwar)
  * [jettyRunWarDebug](#jettyrunwardebug)
  * [jettyStart](#jettystart)
  * [jettyStartDebug](#jettystartdebug)
  * [jettyStartWar](#jettystartwar)
  * [jettyStartWarDebug](#jettystartwardebug)
  * [jettyStop](#jettystop)
  * [jettyRestart](#jettyrestart)
  * [jettyBeforeIntegrationTest](#jettybeforeintegrationtest)
  * [jettyAfterIntegrationTest](#jettyafterintegrationtest)
* [Configuration](#configuration)
* [WAR Overlays](#war-overlays)
* [Security Realms](#security-realms)
* [Hot deployment](#hot-deployment)
* [Debugger support](#debugger-support)
* [Logging](#logging)
* [jetty.xml support](#jettyxml-support)
* [jetty-env.xml support](#jetty-envxml-support)
* [JEE annotations support](#jee-annotations-support)
* [Web fragments support](#web-fragments-support)
* [Integration tests support](#integration-tests-support)
* [Publishing gretty to sonatype and bintray](#publishing-gretty-to-sonatype-and-bintray)
* [Copyright and License](#copyright-and-license)

## Main features:

* [Easy switch between jetty and servlet-API versions](#switching-between-jetty-and-servlet-api-versions). 
  Gretty supports jetty 7, 8 and 9 and servlet API 2.5, 3.0.1 and 3.1.0.
  
* [WAR Overlays](#war-overlays). Gretty can generate WAR overlays automatically, each time you build your project.

* [Security Realms](#security-realms). Gretty supports jetty security realms.

* [Hot deployment](#hot-deployment). Gretty accurately reloads your web-application, as soon as assets or compiled classes have changed.

* [Debugger support](#debugger-support). Gretty integrates nicely with Java IDE debuggers.

* [Logging](#logging). Gretty incorporates logback logging, although it does not force it on your web-application.

* [jetty.xml support](#jettyxml-support). Gretty automatically uses jetty.xml, if it's present.

* [jetty-env.xml support](#jetty-envxml-support). Gretty automatically uses jetty-env.xml, if it's present.

* [JEE annotations support](#jee-annotations-support). Gretty supports JEE annotations out-of-the-box, no effort needed.

* [Web fragments support](#web-fragments-support). Gretty supports META-INF/web-fragment.xml and META-INF/resources
  in dependency libraries - both in inplace and WAR tasks.

* [Integration tests support](#integration-tests-support). Gretty can start jetty before integration tests and stop it after.

## Usage:

Add the following to "build.gradle" of your web-application:

```groovy
apply from: 'https://raw.github.com/akhikhl/gretty/master/pluginScripts/gretty.plugin'
```

then do "gradle jettyRun" from command-line.

Alternatively, you can download the script from https://raw.github.com/akhikhl/gretty/master/pluginScripts/gretty.plugin 
to the project folder and include it like this:

```groovy
apply from: 'gretty.plugin'
```

or feel free copying (and modifying) the declarations from this script to your "build.gradle", like this:

```groovy
buildscript {

  repositories {
    mavenLocal()
    mavenCentral()
  }

  apply plugin: 'maven'
  
  dependencies {
    classpath 'org.akhikhl.gretty:gretty-plugin:+'
  }
}

repositories {
  mavenLocal()
  mavenCentral()
}

apply plugin: 'war'
apply plugin: 'gretty'
```

All versions of gretty are available on maven central under the group 'org.akhikhl.gretty'.

## Switching between jetty and servlet API versions

'gretty.plugin' described above uses the latest version of jetty and latest version of servlet API.

To use specific version of jetty and servlet API, add one of the following to "build.gradle" of your web-application (instead of 'gretty.plugin'):

```groovy
apply from: 'https://raw.github.com/akhikhl/gretty/master/pluginScripts/gretty7.plugin'
```

  This adds jetty 7.6.14.v20131031 and servlet API 2.5 to the web-application.

```groovy
apply from: 'https://raw.github.com/akhikhl/gretty/master/pluginScripts/gretty8.plugin'
```

  This adds jetty 8.1.8.v20121106 and servlet API 3.0.1 to the web-application.

```groovy
apply from: 'https://raw.github.com/akhikhl/gretty/master/pluginScripts/gretty9.plugin'
```

  This adds jetty 9.1.0.v20131115 and servlet API 3.1.0 to the web-application.
  
**Attention:** please *do not* add several variants of grettyX.plugin at the same time. Only one of the abovementioned plugins should be used in the same web-application.

## Supported tasks

### jettyRun

**Syntax:**

```shell
gradle jettyRun
```

**Effect:**

1. The web-application gets compiled (if it's not up-to-date).
2. Embedded jetty starts in separate java process against compiled classes and their dependencies and listens for HTTP-requests on port 8080.
3. Jetty process waits for the user keypress.
4. Gradle script waits for jetty process to complete.
5. When user presses any key (in the same terminal), jetty process shuts down and gradle script continues normal execution of tasks.

Note that this task does not depend on "war" task, nor does it use "war"-file.

### run

**Syntax:**

```shell
gradle run
```

**Effect:**

"run" task is the same as "jettyRun". It was implemented for better integration of gretty with netbeans IDE.

### jettyRunDebug

**Syntax:**

```shell
gradle jettyRunDebug
```

**Effect:**

1. The web-application gets compiled (if it's not up-to-date).
2. Embedded jetty starts in separate java process against compiled classes and their dependencies.
   **important**: Upon start, the process is in suspended-mode and listens for debugger commands on port 5005.
3. Upon resume, jetty process starts listening for HTTP-requests on port 8080 and waiting for the user keypress.
4. Gradle script waits for jetty process to complete.
5. When user presses any key (in the same terminal), jetty process shuts down and gradle script continues normal execution of tasks.

Note that this task does not depend on "war" task, nor does it use "war"-file.

See also: [Debugger support](#debugger-support).

### debug

**Syntax:**

```shell
gradle debug
```

**Effect:**

"debug" task is the same as "jettyRunDebug". It was implemented for better integration of gretty with netbeans IDE.

### jettyRunWar

**Syntax:**

```shell
gradle jettyRunWar
```

**Effect:**

1. The web-application gets compiled and assembled into WAR-file (if it's not up-to-date).
2. Embedded jetty starts in separate java process against WAR-file and listens for HTTP-requests on port 8080.
3. Jetty process waits for the user keypress.
4. Gradle script waits for jetty process to complete.
5. When user presses any key (in the same terminal), jetty process shuts down and gradle script continues normal execution of tasks.

### jettyRunWarDebug

**Syntax:**

```shell
gradle jettyRunWarDebug
```

**Effect:**

1. The web-application gets compiled and assembled into WAR-file (if it's not up-to-date).
2. Embedded jetty starts in separate java process against WAR-file.
   **important**: Upon start, the process is in suspended-mode and listens for debugger commands on port 5005.
3. Upon resume, jetty process starts listening for HTTP-requests on port 8080 and waiting for the user keypress.
4. Gradle script waits for jetty process to complete.
5. When user presses any key (in the same terminal), jetty process shuts down and gradle script continues normal execution of tasks.

See also: [Debugger support](#debugger-support).

### jettyStart

**Syntax:**

```shell
gradle jettyStart
```

**Effect:**

1. The web-application gets compiled (if it's not up-to-date).
2. Embedded jetty starts in separate java process against compiled classes and their dependencies and listens for HTTP-requests on port 8080.
3. Jetty process goes to infinite loop and listens for service signals on port 9900.
4. Gradle script waits for jetty process to complete.
5. When "stop" signal comes, jetty process stops and gradle script continues normal execution of tasks.
6. When "restart" signal comes, jetty process restarts the web-application and continues waiting for signals.

Note that this task does not depend on "war" task, nor does it use "war"-file.

See also: tasks [jettyStop](#jettystop) and [jettyRestart](#jettyRestart).

### jettyStartDebug

**Syntax:**

```shell
gradle jettyStartDebug
```

**Effect:**

1. The web-application gets compiled (if it's not up-to-date).
2. Embedded jetty starts in separate java process against compiled classes and their dependencies.
   **important**: Upon start, the process is in suspended-mode and listens for debugger commands on port 5005.
3. Upon resume, jetty process starts listening for HTTP-requests on port 8080, goes to infinite loop and listens for service signals on port 9900.
4. Gradle script waits for jetty process to complete.
5. When "stop" signal comes, jetty process stops and gradle script continues normal execution of tasks.
6. When "restart" signal comes, jetty process restarts the web-application and continues waiting for signals.

Note that this task does not depend on "war" task, nor does it use "war"-file.

See also: tasks [jettyStop](#jettystop) and [jettyRestart](#jettyRestart).

See also: [Debugger support](#debugger-support).

### jettyStartWar

**Syntax:**

```shell
gradle jettyStartWar
```

**Effect:**

1. The web-application gets compiled and assembled into WAR-file (if it's not up-to-date).
2. Embedded jetty starts in separate java process against WAR-file and listens for HTTP-requests on port 8080.
3. Jetty process goes to infinite loop and listens for service signals on port 9900.
4. Gradle script waits for jetty process to complete.
5. When "stop" signal comes, jetty process stops and gradle script continues normal execution of tasks.
6. When "restart" signal comes, jetty process restarts the web-application and continues waiting for signals.

See also: tasks [jettyStop](#jettystop) and [jettyRestart](#jettyRestart).

### jettyStartWarDebug

**Syntax:**

```shell
gradle jettyStartWarDebug
```

**Effect:**

1. The web-application gets compiled and assembled into WAR-file (if it's not up-to-date).
2. Embedded jetty starts in separate java process against WAR-file.
   **important**: Upon start, the process is in suspended-mode and listens for debugger commands on port 5005.
3. Upon resume, jetty process starts listening for HTTP-requests on port 8080, goes to infinite loop and listens for service signals on port 9900.
4. Gradle script waits for jetty process to complete.
5. When "stop" signal comes, jetty process stops and gradle script continues normal execution of tasks.
6. When "restart" signal comes, jetty process restarts the web-application and continues waiting for signals.

See also: tasks [jettyStop](#jettystop) and [jettyRestart](#jettyRestart).

See also: [Debugger support](#debugger-support).

### jettyStop

**Syntax:**

```shell
gradle jettyStop
```

**Effect:**

Does not build source code whatsoever, only sends "stop" signal to localhost:9900.
This task assumes that jetty was started with "jettyStart" task and listens for signals on the designated port.

### jettyRestart

**Syntax:**

```shell
gradle jettyRestart
```

**Effect:**

Does not build source code whatsoever, only sends "restart" signal to localhost:9900.
This task assumes that jetty was started with "jettyStart" task and listens for signals on the designated port.

### jettyBeforeIntegrationTest

Internal task, please, don't invoke it on command line!

Gretty automatically defines and invokes this task, when you define "integrationTestTask" property.

**Effect:**

1. Embedded jetty starts in separate java process against compiled classes and their dependencies and listens for HTTP-requests on port 8080.
2. Jetty process goes to infinite loop and listens for service signals on port 9900.
3. Gradle script waits until jetty is online.
4. Gradle script proceeds to integration test task.

See also: [integration tests support](#integration-tests-support).

### jettyAfterIntegrationTest

Internal task, please, don't invoke it on command line!

Gretty automatically defines and invokes this task, when you define "integrationTestTask" property.

**Effect:**

1. Sends "stop" signal to localhost:9900.
2. When "stop" signal comes, jetty process stops.
3. Gradle script waits for jetty process to complete.
4. Gradle script continues normal execution of tasks.

See also: [integration tests support](#integration-tests-support).

## Configuration

It's possible to change gretty configuration via plugin extension object:

```groovy
gretty {
  port = 8080
  servicePort = 9900
  contextPath = '/myWebApp'
  initParameter 'param1', 'buildTimeEvaluationParameter'
  initParameter 'param2', { 'lazyEvaluationParameter' }
  jettyXml 'jetty.xml'
  jettyEnvXml 'jetty-env.xml'
  realm 'auth'
  realmConfigFile 'WEB-INF/jetty-realm.properties'
  onStart {
    println 'Jetty start'
  }
  onStop {
    println 'Jetty stop'
  }
  overlay ':ProjectA'
  overlay ':ProjectB'
  scanInterval = 5
  scanDir 'dir1'
  scanDir 'dir2'
  onScan {
    println 'Scanning files for changes'
  }
  onScanFilesChanged { fileList ->
    println "Files were changed: $fileList"
  }
  logbackConfigFile = 'xyz/logback.groovy'
  loggingLevel = 'INFO'
  consoleLogEnabled = true
  fileLogEnabled = true
  logFileName = project.name
  logDir = "${System.getProperty('user.home')}/logs"
  integrationTestTask = 'integrationTest'
  integrationTestStatusPort = 9901
}
```

"port" defines TCP-port used by Jetty for incoming HTTP-requests. Default value is 8080.

"servicePort" defines TCP-port used by gretty-plugin to communicate between jettyStart(War) and jettyStop/jettyRestart tasks.
Default value is 9900.

"contextPath" defines context path for the web-application (defaults to project name). "contextPath" affects 
only jettyRun[War], jettyStart[War] tasks. If you assemble WAR file and deploy it
to some other servlet container, you'll have to define context path by means of that container.

"initParameter" defines web-application initialization parameter. It has the same meaning/effect, 
as /web-app/servlet/init-param element in "web.xml". You can specify more than one initParameter.

"jettyXml" defines name and/or location of "jetty.xml" file. See more information in chapter [jetty.xml support](#jettyxml-support).

"jettyEnvXml" defines name and/or location of "jetty-env.xml" file. See more information in chapter [jetty-env.xml support](#jetty-envxml-support).

"realm" defines security realm for the given web-application. See more information in chapter [Security Realms](#security-realms).

"realmConfigFile" defines properties file, containing security realm properties. See more information in chapter [Security Realms](#security-realms).

"onStart" defines closure to be called just before jetty server is started.

"onStop" defines closure to be called just after jetty server is stopped.

"overlay" defines another project as WAR overlay source. See more information in chapter [WAR Overlays](#war-overlays).

"scanInterval" defines hot-deployment scan interval, in seconds. See more information in chapter [Hot deployment](#hot-deployment).

"scanDir" defines one or more directories, scanned by hot-deployment. See more information in chapter [Hot deployment](#hot-deployment).

"onScan" defines closure to be called on hot-deployment scan. See more information in chapter [Hot deployment](#hot-deployment).

"onScanFilesChanged" defines closure to be called whenever hot-deployment detects that files or folders were changed. See more information in chapter [Hot deployment](#hot-deployment).

"logbackConfigFile" defines the absolute or relative path to logback configuration file (.groovy or .xml). See more information in chapter [Logging](#logging).

"loggingLevel" defines slf4j logging-level for jetty process. See more information in chapter [Logging](#logging).

"consoleLogEnabled" defines, whether log messages are written to the terminal. See more information in chapter [Logging](#logging).

"fileLogEnabled" defines, whether log messages are written to the log-file. See more information in chapter [Logging](#logging).

"logFileName" defines log file name (without path). See more information in chapter [Logging](#logging).

"logDir" defines directory, where log file is created. See more information in chapter [Logging](#logging).

"integrationTestTask" defines name of existing gradle task, which gretty "encloses" with jetty start/stop. 
See more information in chapter [Integration tests support](#integration-tests-support).

"integrationTestStatusPort" defines TCP-port used by gretty-plugin to communicate between jettyBeforeIntegrationTest/jettyAfterIntegrationTest 
tasks and jetty process. Default value is 9901. See more information in chapter [Integration tests support](#integration-tests-support).

## WAR Overlays

"overlay" property has the following effects:

1. When performing jettyRun and jettyStart, runtime classpath of overlay projects is added to the current project.
   Classpath of the current project has priority.

2. When assembling WAR file, overlay projects are added (overlayed) to the current project. The files of the current 
   project have priority over overlay files, this allows to effectively customize web-application.

You can specify more than one overlay.

Note that "overlay" property "understands" only gradle projects (not maven artifacts) as an input. 

gretty contains example program ["helloGrettyOverlay"](https://github.com/akhikhl/gretty/tree/master/examples/helloGrettyOverlay),
which shows minimal working overlay configuration.

## Security realms

"realm" property defines security realm for the given web-application. When defined, it must match 
/web-app/login-config/realm-name element in "web.xml".

"realmConfigFile" property defines relative (to web-application root) path to the properties file, 
containing properties for HashLoginService. See more information at http://wiki.eclipse.org/Jetty/Tutorial/Realms.

"realm" and "realmConfigFile" affect only jettyRun[War], jettyStart[War] tasks. If you assemble WAR file and deploy it
to some other servlet container, you'll have to define security realms by means of that container.

gretty contains example program ["securityRealm"](https://github.com/akhikhl/gretty/tree/master/examples/securityRealm),
which shows minimal working realm configuration.

## Hot deployment

Gretty supports hot deployment: whenever the classes, jars or resources [comprising the web-application] are updated, the web-application automatically restarts itself.

Gretty hot deployment assumes the following defaults:

"scanInterval" is set to zero, that means that hot deployment is disabled. You need to assign it to non-zero value to enable hot deployment.

Hot deployment scans the following folders:

* ${projectDir}/build/classes/main
* ${projectDir}/build/resources/main
* ${projectDir}/src/main/webapp

as well as all dependency jars and overlay-WARs, comprising the web-application.

*Attention*: gretty hot deployment does not detect changes in the source code files. That means: whenever you just save ".java", ".groovy" or resource file - nothing happens. But as soon as you compile things - for example, invoke "build" (or "compileJava" or "compileGroovy" or "processResources") - hot deployment detects changes and restarts web-application.

Current implementation of hot deployment does not detect changes in gretty parameters. That means: whenever you change "build.gradle", containing "gretty" section, you'll need to restart the web-application "by hand", only then the changes will have effect.

You can fine-tune hot deployment by adjusting the following parameters [of gretty plugin extension]:

"scanDir" defines one or more directories, scanned by hot-deployment. The directory could be a string, denoting relative (to the project) or absolute path, or instance of java.io.File class. You should not call "scanDir" for the following folders:

* ${projectDir}/build/classes/main
* ${projectDir}/build/resources/main
* ${projectDir}/src/main/webapp

since hot-deployment already scans these folders by default.

Also you should not call scanDir for dependency jars or overlay WARs - all these things are already scanned by hot deployment.

"onScan" defines closure to be called on hot-deployment scan, i.e. each scanInterval seconds. The function is called unconditionally, regardless of whether hot deployment detects changed files or not.

"onScanFilesChanged" defines closure to be called whenever hot-deployment detects that files or folders were changed. The closure receives List<File> as the parameter, which holds all changed files and folders.

## Debugger support

All jettyRunXXX and jettyStartXXX tasks have their debugging counterparts. For example, there are tasks jettyRun and jettyRunDebug. jettyRun starts web-application "normally", while jettyRunDebug starts web-application in suspended mode and listens for debugger commands on port 5005.

Author of gretty plugin tested debugging only under netbeans IDE. It should also work under eclipse IDE, but it's untested yet.

## Logging

When gretty runs it's tasks, it appends logback-classic to the project's classpath.

Gretty supports configuring slf4j/logback logging in three forms:

1. If you place "logback.groovy" or "logback.xml" to "src/main/resources", it is compiled (copied) to "build/resources/main" folder
  before running any jetty tasks. Gretty auto-discovers logback configuration file in that folder and applies it to jetty process.
  
2. If you place "logback.groovy" or "logback.xml" to arbitrary folder and then reference it by "logbackConfigFile" property,
  gretty applies the specified configuration to jetty process.
  
3. If there's no "logback.groovy" or "logback.xml" file, gretty configures logging with default settings:

  - It enables slf4j logging with level INFO.

  - It configures two appenders: one for console and another for the log file.

  - Log file by default has name "${project.name}.log" and is created in folder "${System.getProperty('user.home')}/logs".

**Attention**: gretty logging is only effective in gretty tasks. Gretty does not reconfigure logging (or libraries) of the compiled WAR-file.

You can fine-tune gretty logging by adjusting the following properties [of gretty plugin extension]:

<a name="logbackconfigfile"></a>
"logbackConfigFile" defines the absolute or relative path to logback configuration file (.groovy or .xml).
If "logbackConfigFile" is relative, it is first combined with projectDir. If the resulting path points to an existing file,
it is used for logback configuration. If not, gretty tries to combine "logbackConfigFile" with each output folder
(typically "${projectDir}/build/classes/main" and "${projectDir}/build/resources/main"). If any resulting path points to an existing file,
it is used for logback configuration.

  - **Attention**: when logback configuration file is used (either auto-discovered or specified via "logbackConfigFile" property),
  other logging properties ("loggingLevel", "consoleLogEnabled", "fileLogEnabled", "logFileName", "logDir") have no effect.

"loggingLevel" defines slf4j logging-level for jetty process. It is a string, having one of the values: 'ALL', 'DEBUG', 'ERROR', 'INFO', 'OFF', 'TRACE', 'WARN'. The default value is 'INFO'.

"consoleLogEnabled" defines, whether log messages are written to the terminal. It is a boolean, default value is "true".

"fileLogEnabled" defines, whether log messages are written to the log-file. It is a boolean, default value is "true".

"logFileName" defines log file name (without path). It is a string, default value is "${project.name}".

"logDir" defines directory, where log file is created. It is a string, default value is "${System.getProperty('user.home')}/logs".

## jetty.xml support

"jetty.xml" is the configuration file for Jetty (for server itself, not for web-application).

The purpose and syntax of "jetty.xml" is documented on [this page](http://wiki.eclipse.org/Jetty/Reference/jetty.xml).

Gretty recognizes and supports "jetty.xml". As soon as Gretty finds existing "jetty.xml" file,
it reads the file and uses it to configure jetty server.
Even if "jetty.xml" is not found, gretty still works - with reasonable defaults
and possible configuration in gretty extension object.

By convension gretty looks for the file name "jetty.xml". You can (but you don't have to) change the file name
by specifying property "jettyXml" in gretty extension object:

```groovy
gretty {
  // ...
  jettyXml = 'someFile.xml'
  // ...
}
```

If explicitly defined jettyXml represents an absolute path, gretty will try to use just that.

If implicitly or explicitly defined jettyXml represents a relative path, gretty tries 
to find corresponding existing file in the following directories:

- $JETTY_HOME/etc

- $project.projectDir

- $project.webAppDir/WEB-INF

- $project.buildDir/classes

- $project.buildDir/resources

- recursively in all abovementioned folders of the referenced overlay projects (if any)

Gretty sources contain example programs demonstrating integration of "jetty.xml" at work:

- [examples/testJettyXml7](https://github.com/akhikhl/gretty/tree/master/examples/testJettyXml7)

- [examples/testJettyXml8](https://github.com/akhikhl/gretty/tree/master/examples/testJettyXml8)

- [examples/testJettyXml9](https://github.com/akhikhl/gretty/tree/master/examples/testJettyXml9)

## jetty-env.xml support

"jetty-env.xml" jetty-env.xml is an optional Jetty file that configures individual webapp. 
The format of jetty-web.xml is the same as jetty.xml - it is an XML mapping of the Jetty API. 

The purpose and syntax of "jetty-env.xml" is documented on [this page](http://wiki.eclipse.org/Jetty/Reference/jetty-env.xml).

Gretty recognizes and supports "jetty-env.xml". As soon as Gretty finds existing "jetty-env.xml" file,
it reads the file and uses it to configure jetty webapp.
Even if "jetty-env.xml" is not found, gretty still works - with reasonable defaults
and possible configuration in gretty extension object.

By convension gretty looks for the file name "jetty-env.xml". You can (but you don't have to) change the file name
by specifying property "jettyEnvXml" in gretty extension object:

```groovy
gretty {
  // ...
  jettyEnvXml = 'someFile.xml'
  // ...
}
```

If explicitly defined jettyEnvXml represents an absolute path, gretty will try to use just that.

If implicitly or explicitly defined jettyEnvXml represents a relative path, gretty tries 
to find corresponding existing file in the following directories:

- $project.projectDir

- $project.webAppDir/WEB-INF

- $project.buildDir/classes

- $project.buildDir/resources

- recursively in all abovementioned folders of the referenced overlay projects (if any)

Gretty sources contain example program demonstrating integration of "jetty-env.xml" at work:

- [examples/testJettyEnvXml](https://github.com/akhikhl/gretty/tree/master/examples/testJettyEnvXml)

## JEE annotations support

Gretty supports JEE annotations out-of-the-box. That means: you don't have to configure anything,
you just use JEE annotations in the web-application:

```java
import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@WebServlet( name="AnnotatedServlet", displayName="Annotated Servlet", urlPatterns = {"/annotations/*"}, loadOnStartup=1)
public class ExampleServlet extends HttpServlet {

  private static final long serialVersionUID = -6506276378398106663L;

  @Override
  protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    // doGet implementation
  }
}
```

Note that jetty-7 flavor of gretty does not support JEE annotations, since it is using servlet-api 2.5.

jetty-8 and jetty-9 flavors of gretty support JEE annotations in the current web-application
and in it's overlays (if any).

Normally gretty looks for JEE-annotated classes in "build/classes" directories belonging to the web-application
classpath. You can change this behavior by inserting the following into "jetty-env.xml":

```xml
<?xml version="1.0"?>
<!DOCTYPE Configure PUBLIC "-//Jetty//Configure//EN" "http://www.eclipse.org/jetty/configure.dtd">
<Configure class="org.eclipse.jetty.webapp.WebAppContext"> 
  <Call name="setContextAttribute">
    <Arg>org.eclipse.jetty.server.webapp.ContainerIncludeJarPattern</Arg>
    <Arg>.*/foo-[^/]*\.jar$|.*/bar-[^/]*\.jar$|.*/classes/.*</Arg>
  </Call>
</Configure>
```

in the example above we specify, that gretty should scan for annotations any jar whose name starts with "foo-" or "bar-", or a directory named "classes".

## Web fragments support

Gretty supports META-INF/web-fragment.xml and META-INF/resources in dependency libraries - both for inplace and WAR tasks.

**Attention:** web fragments feature was introduced in servlet 3.0 specification, therefore it is not available with jetty 7 (and with gretty7).

## Integration tests support

Gretty provides you a very simple way to start jetty before integration tests and stop it after.
All you have to do is to specify "integrationTestTask" property:

```groovy
gretty {
  integrationTestTask = 'integrationTest' // name of existing gradle task
}
```

and then do "gradle integrationTest" from command-line.

As a side effect, gretty starts jetty before the given task and stops it after.

**Attention:** gretty does not define integration test task nor does it augment it in any way. All it does is start and stop jetty.

By default gretty-plugin uses TCP-port 9901 to communicate between jettyBeforeIntegrationTest/jettyAfterIntegrationTest 
tasks and jetty process. You can change this port by assigning "integrationTestStatusPort" property:

```groovy
gretty {
  integrationTestTask = 'integrationTest' // name of existing gradle task
  integrationTestStatusPort = 9902
}
```

See also: tasks [jettyBeforeIntegrationTest](#jettybeforeintegrationtest) and [jettyAfterIntegrationTest](#jettyafterintegrationtest).

## Publishing gretty to sonatype and bintray

Gretty sources are configured for publishing gretty artifacts to sonatype and bintray.
Publishing typically consists of two steps: 

1. Define publishing-specific properties 
2. Invoke publishing task. 

If you are going to publish gretty to sonatype and/or bintray, see [more information here](publishing.md).

## Copyright and License

Copyright 2013-2014 (c) Andrey Hihlovskiy

All versions, present and past, of gretty-plugin are licensed under [MIT license](license.txt).

