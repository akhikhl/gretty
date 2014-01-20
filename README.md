#gretty

Gradle plugin for running web-applications under jetty 7, 8 and 9 and servlet API 2.5, 3.0.1 and 3.1.0.

New in version 0.0.9: out-of-the-box [JEE annotations support](#jee-annotations-support), bug-fixes.

New in version 0.0.8: implemented support of [jetty.xml](#jettyxml-support) and [jetty-env.xml](#jetty-envxml-support).

New in version 0.0.7: implemented accurate re-configuration of logback loggers and appenders on hot-deployment.

New in version 0.0.6: support of [multiple jetty versions and multiple servlet API versions](#switching-between-jetty-and-servlet-API-versions).

New in version 0.0.5: [debugger support](#debugger-support) and [logging](#logging).

New in version 0.0.4: support of [hot deployment](#hot-deployment).

**Content of this document**

* [Usage](#usage)
* [Switching between jetty and servlet API versions](#switching-between-jetty-and-servlet-API-versions)
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
* [Configuration](#configuration)
* [WAR Overlays](#war-overlays)
* [Security Realms](#security-realms)
* [Hot deployment](#hot-deployment)
* [Debugger support](#debugger-support)
* [Logging](#logging)
* [jetty.xml support](#jettyxml-support)
* [jetty-env.xml support](#jetty-envxml-support)
* [JEE annotations support](#jee-annotations-support)
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

All versions of gretty are available in maven central under the group 'org.akhikhl.gretty'.

##Switching between jetty and servlet API versions

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

##Supported tasks

###jettyRun

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

###run

**Syntax:**

```shell
gradle run
```

**Effect:**

"run" task is the same as "jettyRun". It was implemented for better integration of gretty with netbeans IDE.

###jettyRunDebug

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

###debug

**Syntax:**

```shell
gradle debug
```

**Effect:**

"debug" task is the same as "jettyRunDebug". It was implemented for better integration of gretty with netbeans IDE.

###jettyRunWar

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

###jettyRunWarDebug

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

###jettyStart

**Syntax:**

```shell
gradle jettyStart
```

**Effect:**

1. The web-application gets compiled (if it's not up-to-date).
2. Embedded jetty starts in separate java process against compiled classes and their dependencies and listens for HTTP-requests on port 8080.
3. Jetty process goes to infinite loop and listens for service signals on port 9999.
4. Gradle script waits for jetty process to complete.
5. When "shutdown" signal comes, jetty process shuts down and gradle script continues normal execution of tasks.
6. When "restart" signal comes, jetty process restarts the web-application and continues waiting for signals.

Note that this task does not depend on "war" task, nor does it use "war"-file.

See also: tasks [jettyStop](#jettystop) and [jettyRestart](#jettyRestart).

###jettyStartDebug

**Syntax:**

```shell
gradle jettyStartDebug
```

**Effect:**

1. The web-application gets compiled (if it's not up-to-date).
2. Embedded jetty starts in separate java process against compiled classes and their dependencies.
   **important**: Upon start, the process is in suspended-mode and listens for debugger commands on port 5005.
3. Upon resume, jetty process starts listening for HTTP-requests on port 8080, goes to infinite loop and listens for service signals on port 9999.
4. Gradle script waits for jetty process to complete.
5. When "shutdown" signal comes, jetty process shuts down and gradle script continues normal execution of tasks.
6. When "restart" signal comes, jetty process restarts the web-application and continues waiting for signals.

Note that this task does not depend on "war" task, nor does it use "war"-file.

See also: tasks [jettyStop](#jettystop) and [jettyRestart](#jettyRestart).

See also: [Debugger support](#debugger-support).

###jettyStartWar

**Syntax:**

```shell
gradle jettyStartWar
```

**Effect:**

1. The web-application gets compiled and assembled into WAR-file (if it's not up-to-date).
2. Embedded jetty starts in separate java process against WAR-file and listens for HTTP-requests on port 8080.
3. Jetty process goes to infinite loop and listens for service signals on port 9999.
4. Gradle script waits for jetty process to complete.
5. When "shutdown" signal comes, jetty process shuts down and gradle script continues normal execution of tasks.
6. When "restart" signal comes, jetty process restarts the web-application and continues waiting for signals.

See also: tasks [jettyStop](#jettystop) and [jettyRestart](#jettyRestart).

###jettyStartWarDebug

**Syntax:**

```shell
gradle jettyStartWarDebug
```

**Effect:**

1. The web-application gets compiled and assembled into WAR-file (if it's not up-to-date).
2. Embedded jetty starts in separate java process against WAR-file.
   **important**: Upon start, the process is in suspended-mode and listens for debugger commands on port 5005.
3. Upon resume, jetty process starts listening for HTTP-requests on port 8080, goes to infinite loop and listens for service signals on port 9999.
4. Gradle script waits for jetty process to complete.
5. When "shutdown" signal comes, jetty process shuts down and gradle script continues normal execution of tasks.
6. When "restart" signal comes, jetty process restarts the web-application and continues waiting for signals.

See also: tasks [jettyStop](#jettystop) and [jettyRestart](#jettyRestart).

See also: [Debugger support](#debugger-support).

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
  loggingLevel = 'INFO'
  consoleLogEnabled = true
  fileLogEnabled = true
  logFileName = project.name
  logDir = "${System.getProperty('user.home')}/logs"
}
```

"port" defines TCP-port used by Jetty for incoming HTTP-requests.

"stopPort" defines TCP-port used by gretty-plugin to communicate between jettyStart(War) and jettyStop/jettyRestart tasks.

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

"loggingLevel" defines slf4j logging-level for jetty process. See more information in chapter [Logging](#logging).

"consoleLogEnabled" defines, whether log messages are written to the terminal. See more information in chapter [Logging](#logging).

"fileLogEnabled" defines, whether log messages are written to the log-file. See more information in chapter [Logging](#logging).

"logFileName" defines log file name (without path). See more information in chapter [Logging](#logging).

"logDir" defines directory, where log file is created. See more information in chapter [Logging](#logging).

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

##Debugger support

All jettyRunXXX and jettyStartXXX tasks have their debugging counterparts. For example, there are tasks jettyRun and jettyRunDebug. jettyRun starts web-application "normally", while jettyRunDebug starts web-application in suspended mode and listens for debugger commands on port 5005.

Author of gretty plugin tested debugging only under netbeans IDE. It should also work under eclipse IDE, but it's untested yet.

##Logging

Gretty Plugin implements pre-configured logging:

1. It appends logback-classic to the project's classpath.

2. It enables slf4j logging with level INFO.

3. It configures two appenders: one for console and another for the log file.

4. Log file by default has name "${project.name}.log" and is created in folder "${System.getProperty('user.home')}/logs".

**Attention**: gretty logging is only effective in gretty tasks. Gretty does not reconfigure logging (or libs) of the compiled WAR-file.

You can fine-tune gretty logging by adjusting the following parameters [of gretty plugin extension]:

"loggingLevel" defines slf4j logging-level for jetty process. It is a string, having one of the values: 'ALL', 'DEBUG', 'ERROR', 'INFO', 'OFF', 'TRACE', 'WARN'. The default value is 'INFO'.

"consoleLogEnabled" defines, whether log messages are written to the terminal. It is a boolean, default value is "true".

"fileLogEnabled" defines, whether log messages are written to the log-file. It is a boolean, default value is "true".

"logFileName" defines log file name (without path). It is a string, default value is "${project.name}".

"logDir" defines directory, where log file is created. It is a string, default value is "${System.getProperty('user.home')}/logs".

##jetty.xml support

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

##jetty-env.xml support

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

##JEE annotations support

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

##Copyright and License

Copyright 2013-2014 (c) Andrey Hihlovskiy

All versions, present and past, of gretty-plugin are licensed under MIT license:

* [MIT](http://opensource.org/licenses/MIT)


[![Bitdeli Badge](https://d2weczhvl823v0.cloudfront.net/akhikhl/gretty/trend.png)](https://bitdeli.com/free "Bitdeli Badge")

