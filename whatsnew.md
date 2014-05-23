[Project home](https://github.com/akhikhl/gretty) | [Wiki home](../../wiki) | [Feature overview](../../wiki/Feature-overview)

[![logo](http://akhikhl.github.io/gretty/media/gretty_logo.png "gretty logo")](https://github.com/akhikhl/gretty)

### What's new:

### Version 0.0.20

- Fixed issue [No such property: absolutePath for class: java.lang.String error is thrown on jetty* build](https://github.com/akhikhl/gretty/issues/23)

### Version 0.0.19

- Fixed compatibility issue: gretty would not start on JDK7, when taken from maven (not compiled from sources).

### Version 0.0.18

- implemented [multiple web-apps feature](../../wiki/Multiple-web-aps-introduction).

- implemented [debugger support for multiple web-apps](../../wiki/Debugging-a-farm).

- implemented [integration tests support for multiple web-apps](../../wiki/Farm-integration-tests).

- implemented gretty.afterEvaluate and farm.afterEvaluate closures for easy configuration of gretty tasks.

- implemented highly customizable [gretty task classes](../../wiki/Gretty-task-classes) and [farm task classes](../../wiki/Farm-task-classes).

- improved security of realmConfigFile: now it uses "${webAppDir}/WEB-INF" as a base folder, if you specify relative path.

- completely rewritten documentation.

### Version 0.0.17

- fixed incorrect parameter passing to javaexec in GrettyStartTask, preventing debug mode.

### Version 0.0.16

- Reimplemented Gretty tasks as reusable classes.

- Renamed integrationTestStatusPort to [statusPort](../../wiki/Configuration#statusPort).

- Moved documentation from README.md to [wiki pages](../../wiki).

- Updated documentation, added **nice diagrams** to every task description. See more at [wiki pages](../../wiki).

### Version 0.0.15

- Introduced configuration property [fastReload](../../wiki/Configuration#fastReload).

- Fixed JDK-8 compatibility issues.

### Version 0.0.14

- Introduced configuration property [jvmArgs](../../wiki/Configuration#jvmArgs).

### Version 0.0.13

- Implemented [support of web fragments](../../wiki/Web-fragments-support)
- Implemented integration tests for most of the examples
- Introduced bintray publishing configuration in build.gradle

### Version 0.0.12

- Implemented [support of integration tests](../../wiki/Integration-tests-support)

### Version 0.0.11

- Introduced configuration property [logbackConfigFile](../../wiki/Gretty-Configuration#logbackConfigFile)
  (in response to [issue #6](https://github.com/akhikhl/gretty/issues/6) "Possibility to provide custom logback.xml or logback.groovy configuration")

### Version 0.0.10

- Fixed overlay WAR generation.
- Upgraded to logback version 1.1.1 and slf4j version 1.7.6.
- Updated documentation.

### Version 0.0.9

- Implemented out-of-the-box [JEE annotations support](../../wiki/JEE-annotations-support).
- Various bug-fixes.

### Version 0.0.8

- Implemented support of [jetty.xml](../../wiki/jetty.xml-support) and [jetty-env.xml](../../wiki/jetty-env.xml-support).

### Version 0.0.7

- Implemented accurate re-configuration of logback loggers and appenders on hot-deployment.

### Version 0.0.6

- Implemented support of [multiple jetty versions and multiple servlet API versions](../../wiki/Switching-between-jetty-and-servlet-API-versions).

### version 0.0.5

- Implemented [debugger support](../../wiki/Debugger-support) and [logging](../../wiki/Logging).

### Version 0.0.4

- Implemented support of [hot deployment](../../wiki/Hot-deployment).

