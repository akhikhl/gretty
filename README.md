#gretty

Gradle plugin for running web-applications under jetty 8.1.8.

##Usage:

1. Please run "gradle install" in "libs" folder.

2. Please add to your projects "build.gradle":


```groovy
buildscript {

  repositories {
    mavenLocal()
    mavenCentral()
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


