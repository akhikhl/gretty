[Gretty project home](https://github.com/akhikhl/gretty) | [Main features](../../wiki/Main-features) | [Wiki pages](../../wiki)

[![logo](http://akhikhl.github.io/gretty/media/gretty_logo.png "gretty logo")](https://github.com/akhikhl/gretty)

### What's new:

### Version 0.0.16

- Reimplemented Gretty tasks as two reusable classes: [GrettyStartTask](../../wiki/GrettyStartTask) and [GrettyServiceTask](../../wiki/GrettyServiceTask). Don't worry, all task instances are still valid (jettyRun, jettyRunDebug, ...).

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

- Introduced configuration property [logbackConfigFile](../../Configuration#logbackConfigFile)
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

