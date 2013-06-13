#gretty

Gradle plugin for running web-applications under jetty 8.1.8.

##Usage:

1. Run "gradle install" in "libs" folder (at least once).

2. Add the following to your projects "build.gradle":


```groovy
buildscript {

  repositories {
    mavenLocal()
  }

  apply plugin: "maven"
  
  dependencies {
    classpath "org.akhikhl.gretty:gretty-plugin:0.0.1"
  }
}

apply plugin: "java"
apply plugin: "war"
apply plugin: "gretty"

dependencies {
  providedCompile "javax.servlet:javax.servlet-api:3.0.1"
}

```

##Supported tasks

###jettyRun

**Syntax:**

```shell
gradle jettyRun
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


