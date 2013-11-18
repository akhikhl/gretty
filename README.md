#gretty

**version 0.0.3**

Gradle plugin for running web-applications under jetty 8.1.8.

**Content of this document**

* [Usage](#usage)
* [Supported tasks](#supported-tasks)
  * [jettyRun](#jettyrun)
  * [jettyRunWar](#jettyrunwar)
  * [jettyStart](#jettystart)
  * [jettyStartWar](#jettystartwar)
  * [jettyStop](#jettystop)
* [Configuration](#configuration)
* [WAR Overlays](#war-overlays)
* [Copyright and License](#copyright-and-license)

##Usage:

1. Add the following to "build.gradle" of your web-application:


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

The web-application gets compiled (if it's not up-to-date), then embedded jetty is started
against compiled classes and their dependencies and goes online at port 8080. 
Gradle script waits for the user keypress. When user presses any key 
(in the same terminal), jetty shuts down and gradle continues normal execution of tasks.
Note that this task does not depend on "war" task, nor does it use "war"-file.

###jettyRunWar

**Syntax:**

```shell
gradle jettyRunWar
```

**Effect:**

The web-application gets compiled and assembled into WAR-file (if it's not up-to-date), then embedded jetty is started
against WAR-file and goes online at port 8080. Gradle script waits for the user keypress. When user presses any key 
(in the same terminal), jetty shuts down and gradle continues normal execution of tasks.

###jettyStart

**Syntax:**

```shell
gradle jettyStart
```

**Effect:**

The web-application gets compiled (if it's not up-to-date), then embedded jetty is started
against compiled classes and their dependencies and goes online at port 8080. 
Gradle script waits for shutdown signal via port 9999.
When shutdown signal comes, jetty shuts down and gradle continues normal execution of tasks.
Note that this task does not depend on "war" task, nor does it use "war"-file.

###jettyStartWar

**Syntax:**

```shell
gradle jettyStartWar
```

**Effect:**

The web-application gets compiled and assembled into WAR-file (if it's not up-to-date), then embedded jetty is started
against WAR-file and goes online at port 8080. Gradle script waits for shutdown signal via port 9999.
When shutdown signal comes, jetty shuts down and gradle continues normal execution of tasks.

###jettyStop

**Syntax:**

```shell
gradle jettyStop
```

**Effect:**

Does not build source code whatsoever, simply sends jetty shutdown signal to localhost:9999.

##Configuration

It's possible to change gretty configuration via plugin extension object:

```groovy
gretty {
  port = 8081
  stopPort = 9998
  onStart {
    println "Jetty start"
  }
  onStop {
    println "Jetty stop"
  }
  overlay project(":ProjectA")
  overlay project(":ProjectB")
}
```

"port" defines which TCP-port is used by Jetty for incoming HTTP-requests

"stopPort" defines which TCP-port is used by gretty-plugin to communicate between jettyStart(War) and jettyStop tasks.

"onStart" allows to add one or more closures, which will be called just before jetty is started.

"onStop" allows to add one or more closures, which will be called just after jetty is stopped.

"overlay" allows to specify one or more projects as WAR overlay source

##WAR Overlays

Overlay property "understands" only gradle projects as an input. Overlay has the following effect:

1. Runtime classpath of overlay projects is added to the current project, when performing jettyRun, jettyStart. 
   Classpath of the current project has priority.

2. Overlay projects are added (overlayed) to the current project, when assembling WAR file. The files of the current 
   project have priority over overlay files, this allows to effectively customize web-application.

##Copyright and License

Copyright 2013 (c) Andrey Hihlovskiy

All versions, present and past, of gretty-plugin are licensed under MIT license:

* [MIT](http://opensource.org/licenses/MIT)
