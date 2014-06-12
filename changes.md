[Project home](https://github.com/akhikhl/gretty) | [Documentation](http://akhikhl.github.io/gretty-doc/) | [Feature overview](http://akhikhl.github.io/gretty-doc/Feature-overview.html)

[![logo](http://akhikhl.github.io/gretty-doc/images/gretty_logo.png "gretty logo")](https://github.com/akhikhl/gretty)

### What's new:

#### Version 0.0.25

- Adopted new plugin identification scheme suggested at the portal [http://plugins.gradle.org](http://plugins.gradle.org/submit).
 See more information at [Gretty plugin names](http://akhikhl.github.io/gretty-doc/Gretty-plugin-names.html).

- Upgraded to Jetty 9.2.1.v20140609. See [Jetty Release 9.2.0 announcement](http://dev.eclipse.org/mhonarc/lists/jetty-announce/msg00065.html)
 and [Jetty 9.2.1.v20140609 release announcement](http://dev.eclipse.org/mhonarc/lists/jetty-announce/msg00066.html) for technical details
 on new Jetty version.
 
- Upgraded to Spring Boot 1.1.1.RELEASE.

#### Version 0.0.24

- Implemented [spring-boot support](http://akhikhl.github.io/gretty-doc/spring-boot-support.html).

- Improved compatibility with JRE-6.

#### Version 0.0.23

- Implemented [HTTPS support](http://akhikhl.github.io/gretty-doc/HTTPS-support.html).

- Introduced new properties in [Gretty configuration](http://akhikhl.github.io/gretty-doc/Gretty-configuration.html) and [Farm configuration](http://akhikhl.github.io/gretty-doc//Farm-server-specific-properties.html), related to HTTPS protocol.

- Introduced convenience functions ServerConfig.jvmArgs, ServerConfig.jvmArg to simplify adding JVM arguments.

#### Version 0.0.22

- Implemented [Jacoco code coverage support](http://akhikhl.github.io/gretty-doc/Code-coverage-support.html) - both server-side and client-side.

#### Version 0.0.21

- Fixed issue [gretty-farm plugin throws IllegalStateException: zip file closed](https://github.com/akhikhl/gretty/issues/24)

- Fixed issue [gretty-farm plugin throws ExecException (jdk1.7.0_55)](https://github.com/akhikhl/gretty/issues/25)

- Fixed bug: stack-overflow exception when warResourceBase is assigned to java.io.File

#### Version 0.0.20

- Fixed issue [No such property: absolutePath for class: java.lang.String error is thrown on jetty* build](https://github.com/akhikhl/gretty/issues/23)

#### Version 0.0.19

- Fixed compatibility issue: gretty would not start on JDK7, when taken from maven (not compiled from sources).

#### Version 0.0.18

- implemented [multiple web-apps feature](http://akhikhl.github.io/gretty-doc/Multiple-web-apps-introduction.html).

- implemented [debugger support for multiple web-apps](http://akhikhl.github.io/gretty-doc/Debugging-a-farm.html).

- implemented [integration tests support for multiple web-apps](http://akhikhl.github.io/gretty-doc/Farm-integration-tests.html).

- implemented gretty.afterEvaluate and farm.afterEvaluate closures for easy configuration of gretty tasks.

- implemented highly customizable [gretty task classes](http://akhikhl.github.io/gretty-doc/Gretty-task-classes.html) and [farm task classes](http://akhikhl.github.io/gretty-doc/Farm-task-classes.html).

- improved security of realmConfigFile: now it uses "${webAppDir}/WEB-INF" as a base folder, if you specify relative path.

- completely rewritten documentation.

#### Version 0.0.17

- fixed incorrect parameter passing to javaexec in GrettyStartTask, preventing debug mode.

#### Version 0.0.16

- Reimplemented Gretty tasks as reusable classes.

- Renamed integrationTestStatusPort to [statusPort](http://akhikhl.github.io/gretty-doc/Gretty-configuration.html#_statusport).

- Moved documentation from README.md to [wiki pages](../../wiki/).

- Updated documentation, added **nice diagrams** to every task description. See more at [wiki pages](../../wiki/).

#### Version 0.0.15

- Introduced configuration property [fastReload](http://akhikhl.github.io/gretty-doc/Gretty-configuration.html#_fastreload).

- Fixed JDK-8 compatibility issues.

#### Version 0.0.14

- Introduced configuration property [jvmArgs](http://akhikhl.github.io/gretty-doc/Gretty-configuration.html#_jvmargs).

#### Version 0.0.13

- Implemented [support of web fragments](http://akhikhl.github.io/gretty-doc/Web-fragments-support.html)
- Implemented integration tests for most of the examples
- Introduced bintray publishing configuration in build.gradle

#### Version 0.0.12

- Implemented [support of integration tests](http://akhikhl.github.io/gretty-doc/Integration-tests-support.html)

#### Version 0.0.11

- Introduced configuration property [logbackConfigFile](http://akhikhl.github.io/gretty-doc/Gretty-configuration.html#_logbackconfigfile)
  (in response to [issue #6](https://github.com/akhikhl/gretty/issues/6) "Possibility to provide custom logback.xml or logback.groovy configuration")

#### Version 0.0.10

- Fixed overlay WAR generation.
- Upgraded to logback version 1.1.1 and slf4j version 1.7.6.
- Updated documentation.

#### Version 0.0.9

- Implemented out-of-the-box [JEE annotations support](http://akhikhl.github.io/gretty-doc/JEE-annotations-support.html).
- Various bug-fixes.

#### Version 0.0.8

- Implemented support of [jetty.xml](http://akhikhl.github.io/gretty-doc/jetty.xml-support.html) and [jetty-env.xml](http://akhikhl.github.io/gretty-doc/jetty-env.xml-support.html).

#### Version 0.0.7

- Implemented accurate re-configuration of logback loggers and appenders on hot-deployment.

#### Version 0.0.6

- Implemented support of [multiple jetty versions and multiple servlet API versions](http://akhikhl.github.io/gretty-doc/Switching-between-Jetty-and-servlet-API-versions.html).

### version 0.0.5

- Implemented [debugger support](http://akhikhl.github.io/gretty-doc/Debugger-support.html) and [logging](http://akhikhl.github.io/gretty-doc/Logging.html).

#### Version 0.0.4

- Implemented [hot deployment](http://akhikhl.github.io/gretty-doc/Hot-deployment.html).

