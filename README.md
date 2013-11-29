#gretty

**version 0.0.4**

Gradle plugin for running web-applications under jetty 8.1.8.

NEW in version 0.0.4: support of [hot deployment](#hot-deployment).

**Content of this document**

* [Usage](#usage)
* [Supported tasks](#supported-tasks)
  * [jettyRun](#jettyrun)
  * [jettyRunWar](#jettyrunwar)
  * [jettyStart](#jettystart)
  * [jettyStartWar](#jettystartwar)
  * [jettyStop](#jettystop)
  * [jettyRestart](#jettyrestart)
* [Configuration](#configuration)
* [WAR Overlays](#war-overlays)
* [Security Realms](#security-realms)
* [Hot deployment](#hot-deployment)
* [Copyright and License](#copyright-and-license)

##Usage:

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

or feel free copying (and modifying) the declarations from this script to your "build.gradle".

##Supported tasks

###jettyRun

**Syntax:**

```shell
gradle jettyRun
```

**Effect:**

1. The web-application gets compiled (if it's not up-to-date).
2. Embedded jetty is started against compiled classes and their dependencies and listens for HTTP-requests on port 8080.
3. Gradle script waits for the user keypress. 
4. When user presses any key (in the same terminal), jetty shuts down and gradle continues normal execution of tasks.

Note that this task does not depend on "war" task, nor does it use "war"-file.

###jettyRunWar

**Syntax:**

```shell
gradle jettyRunWar
```

**Effect:**

1. The web-application gets compiled and assembled into WAR-file (if it's not up-to-date).
2. Embedded jetty is started against WAR-file and listens for HTTP-requests on port 8080.
3. Gradle script waits for the user keypress. 
4. When user presses any key (in the same terminal), jetty shuts down and gradle continues normal execution of tasks.

###jettyStart

**Syntax:**

```shell
gradle jettyStart
```

**Effect:**

1. The web-application gets compiled (if it's not up-to-date).
2. Embedded jetty is started against compiled classes and their dependencies and listens for HTTP-requests on port 8080.
3. Gradle script goes to infinite loop and listens for service signals on port 9999.
4. When "shutdown" signal comes, jetty shuts down and gradle continues normal execution of tasks.
5. When "restart" signal comes, jetty restarts the web-application and continues waiting for signals.

Note that this task does not depend on "war" task, nor does it use "war"-file.

See also: tasks [jettyStop](#jettystop) and [jettyRestart](#jettyRestart).

###jettyStartWar

**Syntax:**

```shell
gradle jettyStartWar
```

**Effect:**

1. The web-application gets compiled and assembled into WAR-file (if it's not up-to-date).
2. Embedded jetty is started against WAR-file and listens for HTTP-requests on port 8080.
3. Gradle script goes to infinite loop and listens for service signals on port 9999.
4. When "shutdown" signal comes, jetty shuts down and gradle continues normal execution of tasks.
5. When "restart" signal comes, jetty restarts the web-application and continues waiting for signals.

See also: tasks [jettyStop](#jettystop) and [jettyRestart](#jettyRestart).

###jettyStop

**Syntax:**

```shell
gradle jettyStop
```

**Effect:**

Does not build source code whatsoever, only sends "shutdown" signal to localhost:9999.
This task assumes that jetty was started with "jettyStart" task and listens for signals on the designated port.

###jettyRestart

**Syntax:**

```shell
gradle jettyRestart
```

**Effect:**

Does not build source code whatsoever, only sends "restart" signal to localhost:9999.
This task assumes that jetty was started with "jettyStart" task and listens for signals on the designated port.

##Configuration

It's possible to change gretty configuration via plugin extension object:

```groovy
gretty {
  port = 8081
  stopPort = 9998
  contextPath = '/myWebApp'
  initParameter 'param1', 'buildTimeEvaluationParameter'
  initParameter 'param2', { 'lazyEvaluationParameter' }
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
}
```

"port" defines TCP-port used by Jetty for incoming HTTP-requests.

"stopPort" defines TCP-port used by gretty-plugin to communicate between jettyStart(War) and jettyStop/jettyRestart tasks.

"contextPath" defines context path for the web-application (defaults to project name). "contextPath" affects 
only jettyRun[War], jettyStart[War] tasks. If you assemble WAR file and deploy it
to some other servlet container, you'll have to define context path by means of that container.

"initParameter" defines web-application initialization parameter. It has the same meaning/effect, 
as /web-app/servlet/init-param element in "web.xml". You can specify more than one initParameter.

"realm" defines security realm for the given web-application. See more information in chapter [Security Realms](#security-realms).

"realmConfigFile" defines properties file, containing security realm properties. See more information in chapter [Security Realms](#security-realms).

"onStart" defines closure to be called just before jetty server is started.

"onStop" defines closure to be called just after jetty server is stopped.

"overlay" defines another project as WAR overlay source. See more information in chapter [WAR Overlays](#war-overlays).

"scanInterval" defines hot-deployment scan interval, in seconds. See more information in chapter [Hot deployment](#hot-deployment).

"scanDir" defines one or more directories, scanned by hot-deployment. See more information in chapter [Hot deployment](#hot-deployment).

"onScan" defines closure to be called on hot-deployment scan. See more information in chapter [Hot deployment](#hot-deployment).

"onScanFilesChanged" defines closure to be called whenever hot-deployment detects that files or folders were changed. See more information in chapter [Hot deployment](#hot-deployment).

##WAR Overlays

"overlay" property has the following effects:

1. When performing jettyRun and jettyStart, runtime classpath of overlay projects is added to the current project.
   Classpath of the current project has priority.

2. When assembling WAR file, overlay projects are added (overlayed) to the current project. The files of the current 
   project have priority over overlay files, this allows to effectively customize web-application.

You can specify more than one overlay.

Note that "overlay" property "understands" only gradle projects (not maven artifacts) as an input. 

gretty contains example program ["helloGrettyOverlay"](https://github.com/akhikhl/gretty/tree/master/examples/helloGrettyOverlay),
which shows minimal working overlay configuration.

##Security realms

"realm" property defines security realm for the given web-application. When defined, it must match 
/web-app/login-config/realm-name element in "web.xml".

"realmConfigFile" property defines relative (to web-application root) path to the properties file, 
containing properties for HashLoginService. See more information at http://wiki.eclipse.org/Jetty/Tutorial/Realms.

"realm" and "realmConfigFile" affect only jettyRun[War], jettyStart[War] tasks. If you assemble WAR file and deploy it
to some other servlet container, you'll have to define security realms by means of that container.

gretty contains example program ["securityRealm"](https://github.com/akhikhl/gretty/tree/master/examples/securityRealm),
which shows minimal working realm configuration.

##Hot deployment

Gretty supports hot deployment: whenever the classes, jars or resources [comprising the web-application] are updated, the web-application automatically restarts itself.

Gretty hot deployment assumes the following defaults:

"scanInterval" is set to zero, that means that hot deployment is disabled. You need to assign it to non-zero value to enable hot deployment.

Hot deployment scans the following folders:
${projectDir}/build/classes/main
${projectDir}/build/resources/main
${projectDir}/src/main/webapp
as well as all dependency jars and overlay-WARs, comprising the web-application.

Attention: gretty hot deployment does not detect changes in the source code files. That means: whenever you just save ".java", ".groovy" or resource file - nothing happens. But as soon as you compile things - for example, invoke "build" (or "compileJava" or "compileGroovy" or "processResources") - hot deployment detects changes and restarts web-application.

Current implementation of hot deployment does not detect changes in gretty parameters. That means: whenever you change "build.gradle", containing "gretty" section, you'll need to restart the web-application "by hand", only then the changes will have effect.

Here is more information on some of the parameters [of gretty plugin extension] affecting hot-deployment:

"scanDir" defines one or more directories, scanned by hot-deployment. The directory could be a string, denoting relative (to the project) or absolute path, or instance of java.io.File class. You don't need to call "scanDir" for the following folders:
${projectDir}/build/classes/main
${projectDir}/build/resources/main
${projectDir}/src/main/webapp
since hot-deployment already scans these folders by default.
Also you don't need to call scanDir for dependency jars or overlay WARs - all these things are already scanned by hot deployment.

"onScan" defines closure to be called on hot-deployment scan, i.e. each scanInterval seconds. The function is called unconditionally, regardless of whether hot deployment detects changed files or not.

"onScanFilesChanged" defines closure to be called whenever hot-deployment detects that files or folders were changed. The closure receives List<File> as the parameter, which holds all changed files and folders.

##Copyright and License

Copyright 2013 (c) Andrey Hihlovskiy

All versions, present and past, of gretty-plugin are licensed under MIT license:

* [MIT](http://opensource.org/licenses/MIT)
