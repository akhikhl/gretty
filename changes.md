[Project home](https://github.com/akhikhl/gretty) | [Documentation](http://akhikhl.github.io/gretty-doc/) | [Feature overview](http://akhikhl.github.io/gretty-doc/Feature-overview.html)

[![logo](http://akhikhl.github.io/gretty-doc/images/gretty_logo.png "gretty logo")](https://github.com/akhikhl/gretty)

### Version 2.0.0

* Compatibility with Gradle 4.0

* Support of Jetty 9.4 (issue #365).

* Now it's possible to override versions of Jetty, Tomcat and servlet API via properties in "gradle.properties" file (issue #330).

* All integration tests now run against Firefox 54.

* Fixed product generation.

* Support of Spring Framework 4.3.9 and Spring Boot 1.5.4.

* Dropped support of Java 6.

### Version 1.4.2

* Now Gretty is compatible with Gradle versions from 2.14.1 to 3.4.1

* resolved issue #326: Gradle 3.3 compatibility work breaks earlier versions of Gradle

* resolved issue #329: Cannot call TaskInputs.property(String, Object) on task ':jettyStart' after task has started execution

### Version 1.4.1

* The most significant change is compatibility with Gradle 3.3.

* Pull Requests #198, #205, #266, #293, #297 and #312 were merged into master.

* Issues #296, #300, #307, #317, #320, #322, #323 were resolved.

### Version 1.4.0

1. All changes by @saladinkzn were merged back to mainstream.

2. Upgraded gradle wrapper to 2.14.1 (https://github.com/akhikhl/gretty/pull/276)

2. Component upgrades (https://github.com/akhikhl/gretty/pull/277)

3. Fixed failing springBootWebSocket example (https://github.com/akhikhl/gretty/pull/278)

4. Fixed issue #247: Could not find property 'main' on task ':run' (https://github.com/akhikhl/gretty/pull/279)

### Version 1.3.0

 1. New scanner for jdk 7+

 2. Fix for: akhikhl#239 (Gretty service port)

 3. Fix for: akhikhl#195 (Possibility to run multiple gretty builds on different ports)

 4. Support overlays for war webapps in farms #4

 5. Update jetty to 9.3 (by providing separate 'container' because latest jetty 9.3 is compiled with jdk 8)

 6. Add support for inserting paths before application output folders (will fix #6)

 7. Add support for dependencies option in farm webapp configuration. #13

 8. New feature: akhikhl#262 (Changing product name)

 9. Fix for duplicate entries in getResources(path) in tomcat 7 and 8 #20

### Version 1.2.5

1. scanDependencies property is working again.

2. redeployMode=redeploy support to redeploy changed web applications only.

3. Jetty is updated to version 9.2.15

4. Spring loaded was updated to 1.2.5.RELEASE

5. Merged fix for `contextPath = '/'` in tomcat

6. Merged patch replacing groovy-all dependency with groovy modules

7. Fixed issue setting keepAliveTimeout

8. Added support for NeedClientAuth param

### Version 1.2.4

1. Properties servicePort and statusPort are deprecated and not used anymore. Gretty automatically selects available TCP ports for communication with it's runner process.

2. Classpath isolation improved. If your web-app uses Groovy, you are free to choose whatever version of it - it will not conflict with the Groovy version used by Gretty runner.

3. Resolved issue #185 "No warning when port service is already in use"

4. Resolved issue #182 "Unable to use log4j-slf4j-impl"

5. Resolved issue #178 "Classpath with property files added to the jetty"

6. Resolved issue #173 "webappCopy doesn't seem to setup inputs/outputs"

7. Resolved issue #145 "Logging via log4j backend doesn't work"

8. Resolved issue #42 "allow to specify custom groovyVersion"

9. Upgraded to Tomcat 7.0.62 and Tomcat 8.0.23

### Version 1.2.3

1. Resolved issue #175 "SpringBoot applications are not isolated from one another in farm and product runs"

2. Resolved issues with logging: #164, #145, #133

3. Upgraded to Tomcat 7.0.61 and Tomcat 8.0.22

4. Upgrade and tests on Gradle 2.4 (Gradle versions >= 1.10 are still supported)

### Version 1.2.2

This is a maintenance release, fixing a single [issue #163](https://github.com/akhikhl/gretty/issues/152): MissingPropertyException when running Tomcat with HTTPS.

### Version 1.2.1

Gretty version 1.2.1 introduces the following changes:

1. Inheriting logging options to generated product is fixed ([issue #152](https://github.com/akhikhl/gretty/issues/152)).

2. Gretty now provides out-of-the-box websocket support ([issue #155](https://github.com/akhikhl/gretty/issues/155)) and examples of using websocket:
  * https://github.com/akhikhl/gretty/blob/master/examples/websocket
  * https://github.com/akhikhl/gretty/blob/master/examples/springBootWebSocket

3. spring-loaded failure under java 8u40 is fixed ([issue #156](https://github.com/akhikhl/gretty/issues/156)).

4. SpringBoot 1.2.x incompatibility with Jetty 8-9 is fixed ([issue #158](https://github.com/akhikhl/gretty/issues/158)).

5. Fixed gretty.baseURI property in integration tests: now it returns "localhost" for all supported servlet containers ([issue #160](https://github.com/akhikhl/gretty/issues/160)).

6. Upgraded to latest components:

```
tomcat8: 8.0.20 -> 8.0.21
slf4j-api: 1.7.7 -> 1.7.12
logback: 1.1.2 -> 1.1.3
spring-boot: 1.1.9.RELEASE -> 1.2.3.RELEASE
spring-loaded: 1.2.1.RELEASE -> 1.2.3.RELEASE
```

### Version 1.2.0

Gretty version 1.2.0 introduces the following changes:

* Upgrade to Jetty 9.2.9.v20150224, which fixes [critical vulnerability CVE-2015-2080](https://github.com/eclipse/jetty.project/blob/master/advisories/2015-02-24-httpparser-error-buffer-bleed.md)
* Fix for class reloading with WAR tasks. Now if some class is changed (recompiled), the web-application restarts as expected.
* Fix for the bug: slf4j/logback libraries are excluded from webapps packed into Gretty product.

### Version 1.1.9

Gretty version 1.1.9 introduces full isolation of it's own logging system (slf4j/logback)
from the constituent web-applications. Now web-applications are free to use any logging system -
Gretty does not interfere and does not force logback to be used.

Also Gretty 1.1.9 solves problems with farm configuration when farm is defined in the parent project
and web-applications are defined in child projects.

At last Gretty 1.1.9 includes upgrades of the following components to their latest versions:

```
jetty7: 7.6.15.v20140411 -> 7.6.16.v20140903
jetty9: 9.2.3.v20140905 -> 9.2.7.v20150116
selenium: 2.44.0 -> 2.45.0
spock: 0.7-groovy-2.0 -> 1.0-groovy-2.4
tomcat7: 7.0.55 -> 7.0.59
tomcat8: 8.0.15 -> 8.0.20
```

### Version 1.1.8

Gretty version 1.1.8 introduces new parameters for [fine-tuning of debugging tasks](http://akhikhl.github.io/gretty-doc/Debugger-support.html#_fine_tuning).

Also Gretty version 1.1.8 includes upgrades of the following components to their latest versions:

- Geb : 0.9.3 -> 0.10.0
- gradle-bintray-plugin : 0.4 -> 1.0
- Gradle Wrapper : 2.1 -> 2.1.1
- Groovy : 2.3.7 -> 2.3.8
- Tomcat-8 : 8.0.14 -> 8.0.15
- Selenium : 2.43.1 -> 2.44.0
- Spring Boot : 1.1.8.RELEASE -> 1.1.9.RELEASE

### Version 1.1.7

New feature: [redirect filter](http://akhikhl.github.io/gretty-doc/Redirect-filter.html).

The feature is completely independent from the rest of Gretty and can be deployed as part of WAR-file.
Charming thing about redirect filter is that it provides groovy-based configuration DSL.

Resolved issue #102: [How to do integration test in a multi-project setup](https://github.com/akhikhl/gretty/issues/102).

Resolved issue with host name in generated certificates, when gretty config does not define sslHost property.

### Version 1.1.6

Maintenance release. Fixed bug: "readonly property" exception when trying to generate Gretty product.

Gretty version 1.1.5 brings new bug, preventing Gretty product generation.
If you run buildProduct task and experience "readonly property" exception,
please switch to Gretty 1.1.6 - it fixes the problem.

### Version 1.1.5

- New feature: [composite farms](http://akhikhl.github.io/gretty-doc/Composite-farms.html).

- New feature [dependent projects can run in inplace mode](http://akhikhl.github.io/gretty-doc/Hot-deployment.html#_dependencyprojectsinplaceserve).

- New feature: [override of context path in integration test tasks](http://akhikhl.github.io/gretty-doc/Override-context-path-in-integration-test-tasks.html).

- New feature: [injection of version variables into project.ext](http://akhikhl.github.io/gretty-doc/Injection-of-version-variables.html).

- Upgraded to Spring Boot 1.1.8.RELEASE.

- Fixed bug: spring-boot improperly shutdown in SpringBootServerManager.stopServer.

- Resolved issue #101: [Jetty.xml Rewrite Handler doesnt seem to take effect](https://github.com/akhikhl/gretty/issues/101).

- Resolved issue #97: [How can I add runner libraries](https://github.com/akhikhl/gretty/issues/97).

- Resolved issue #96: [Custom builds of Gradle cause NumberFormatException](https://github.com/akhikhl/gretty/issues/96).

- Resolved issue #93: [Groovy version conflicts when running farmStart with a war file](https://github.com/akhikhl/gretty/issues/93).

### Version 1.1.4

- New feature: [inplaceMode property](http://akhikhl.github.io/gretty-doc/Gretty-configuration.html#_inplacemode), when assigned to "hard", instructs Gretty to serve files directly from src/main/webapp, bypassing file copy on change.

- New feature: [runner arguments](http://akhikhl.github.io/gretty-doc/Runner-arguments.html) for Gretty products.

- New feature: [interactiveMode property](http://akhikhl.github.io/gretty-doc/Gretty-configuration.html#_interactivemode) allows to fine-tune Gretty's reaction on keypresses.

- New feature: archiveProduct task, archives the generated product to zip-file.

- New feature: [gretty.springBootVersion property](http://akhikhl.github.io/gretty-doc/Gretty-configuration.html#_springbootversion) allows to specify spring boot version (the default is 1.1.7.RELEASE) (issue #88, "Set Spring / SpringBoot version doesn't work").

- New feature: [gretty.enableNaming property](http://akhikhl.github.io/gretty-doc/Gretty-configuration.html#_enablenaming) allows to enable JNDI naming on Tomcat (issue #64, "JNDI - NoInitialContextException with Tomcat (tried in 7x and 8x)").

- Enhancement: now [gretty.jvmArgs property](http://akhikhl.github.io/gretty-doc/Gretty-configuration.html#_jvmargs) is automatically passed to Gretty products.

- Enhancement in Jetty/Windows-specific lifecycle: useFileMappedBuffer is set to false for all Gretty tasks, so that Jetty does not lock css/js files.

- Enhancement in buildProduct task: now it automatically generates VERSION.txt file with the version and creation date information.

- Resolved issue #89, "How to configure fastReload?".

- Upgrades:
  - gradle wrapper to version 2.1
  - Groovy to version 2.3.7
  - SpringBoot to version 1.1.7.RELEASE
  - Embedded Tomcat 7 to version 7.0.55
  - Embedded Tomcat 8 to version 8.0.14
  - Embedded Jetty 9 to version 9.2.3.v20140905
  - asm to version 5.0.3

- Implemented support of Gradle 1.10 (still, using Gradle 2.1 is highly recommended!).

- fixed issues with groovy-all versions and logback versions in the webapp classpath

### Version 1.1.3

- New feature: [virtual mapping of gradle dependencies](http://akhikhl.github.io/gretty-doc/Web-app-virtual-webinflibs.html) (of the web-application) to "WEB-INF/lib" directory. This feature is needed by web frameworks accessing jar files in "WEB-INF/lib" (e.g. Freemarker).

- Fix for compatibility problem with Gradle 1.12 and introduction of Gradle version check.

### Version 1.1.2

- New feature: [webapp extra resource bases](http://akhikhl.github.io/gretty-doc/Web-app-extra-resource-bases.html).

- New feature [webapp filtering](http://akhikhl.github.io/gretty-doc/Web-app-filtering.html).

- Better start/stop protocol, gracefully handling attempts to start Gretty twice (on the same ports). There should be no hanging processes after such attempts anymore.

- gretty.host now defaults to "0.0.0.0", effectively allowing to connect to any interface.

- Fixed issues: #41, #44, #45, #49, #52, #53, #54, #56, #57, #60, #61.

### Version 1.1.1

- Fixed breaking change in 1.1.0: properties jettyXmlFile and jettyEnvXmlFile are supported again (although deprecated, please use serverConfigFile and contextConfigFile properties instead).

- Changed the default value of [managedClassReload property](http://akhikhl.github.io/gretty-doc/Gretty-configuration.html#_managedclassreload) to false. Please set it to true, if you need springloaded integration.

### Version 1.1.0

- New feature: [generation of self-contained runnable products](http://akhikhl.github.io/gretty-doc/Product-generation.html).

- New feature: support of tomcat-specific [server.xml](http://akhikhl.github.io/gretty-doc/tomcat.xml-support.html) and [context.xml](http://akhikhl.github.io/gretty-doc/tomcat-context.xml-support.html) - in Gretty tasks as well as in generated products.

- New feature: [single sign-on](http://akhikhl.github.io/gretty-doc/single-sign-on.html) with Jetty security realms and Tomcat security realms.

- New properties for finer control of hot deployment feature: recompileOnSourceChange, reloadOnClassChange, reloadOnConfigChange, reloadOnLibChange. See more information at [hot deployment](http://akhikhl.github.io/gretty-doc/Hot-deployment.html)

- Upgraded Gretty to Jetty 7.6.15.v20140411, Jetty 9.2.1.v20140609, Tomcat 8.0.9 and Spring Boot 1.1.4.RELEASE. Note that Gretty *was not* upgraded to Jetty 8.1.15.v20140411 because this release brings some strange errors not reproducible with other releases of Jetty 8.

:bell: Attention

Gretty 1.1.0 brings one little incompatibility: property jettyEnvXml was renamed to jettyEnvXmlFile. If you are using jettyEnvXml, please adjust your gradle scripts accordingly.

### Version 1.0.0

- Unified all Gretty plugins to a single plugin "org.akhikhl.gretty".

- Introduced [servlet container selection via servletContainer property](http://akhikhl.github.io/gretty-doc/Switching-between-servlet-containers.html).

- Added support of [Tomcat 7 and 8](http://akhikhl.github.io/gretty-doc/Switching-between-servlet-containers.html).

- Introduced servlet-container-agnostic tasks appRun, appRunDebug, ..., as well as servlet-container-specific tasks jettyRun, jettyRunDebug, ..., tomcatRun, tomcatRunDebug, ...

- Facilitated all web-apps with [spring-loaded](https://github.com/spring-projects/spring-loaded) by default. This can be turned off by setting `managedClassReload=false` in Gretty configuration.

- Hot-deployment property [scanInterval](http://akhikhl.github.io/gretty-doc/Gretty-configuration.html#_scaninterval) is set to 1 (second) by default. Hot-deployment can be turned off by setting `scanInterval=0` in Gretty configuration.

- Hot-deployment property [fastReload](http://akhikhl.github.io/gretty-doc/Gretty-configuration.html#_fastreload) is set to true by default. Fast reloading can be turned off by setting `fastReload=false` in Gretty configuration.

- Added start task functions [prepareServerConfig and prepareWebAppConfig](http://akhikhl.github.io/gretty-doc/Gretty-task-classes.html#_property_inheritance_override) for property inheritance override in gretty tasks.

### Version 0.0.25

- Adopted new plugin identification scheme suggested at the portal [http://plugins.gradle.org](http://plugins.gradle.org).
 See more information at [Gretty gradle plugins](http://akhikhl.github.io/gretty-doc/Gretty-gradle-plugins.html).

- Upgraded to Jetty 9.2.1.v20140609. See [Jetty Release 9.2.0 announcement](http://dev.eclipse.org/mhonarc/lists/jetty-announce/msg00065.html)
 and [Jetty 9.2.1.v20140609 release announcement](http://dev.eclipse.org/mhonarc/lists/jetty-announce/msg00066.html) for technical details
 on new Jetty version.

- Upgraded to Spring Boot 1.1.1.RELEASE.

### Version 0.0.24

- Implemented [spring-boot support](http://akhikhl.github.io/gretty-doc/spring-boot-support.html).

- Improved compatibility with JRE-6.

### Version 0.0.23

- Implemented [HTTPS support](http://akhikhl.github.io/gretty-doc/HTTPS-support.html).

- Introduced new properties in [Gretty configuration](http://akhikhl.github.io/gretty-doc/Gretty-configuration.html) and [Farm configuration](http://akhikhl.github.io/gretty-doc//Farm-server-specific-properties.html), related to HTTPS protocol.

- Introduced convenience functions ServerConfig.jvmArgs, ServerConfig.jvmArg to simplify adding JVM arguments.

### Version 0.0.22

- Implemented [Jacoco code coverage support](http://akhikhl.github.io/gretty-doc/Code-coverage-support.html) - both server-side and client-side.

### Version 0.0.21

- Fixed issue [gretty-farm plugin throws IllegalStateException: zip file closed](https://github.com/akhikhl/gretty/issues/24)

- Fixed issue [gretty-farm plugin throws ExecException (jdk1.7.0_55)](https://github.com/akhikhl/gretty/issues/25)

- Fixed bug: stack-overflow exception when warResourceBase is assigned to java.io.File

### Version 0.0.20

- Fixed issue [No such property: absolutePath for class: java.lang.String error is thrown on jetty* build](https://github.com/akhikhl/gretty/issues/23)

### Version 0.0.19

- Fixed compatibility issue: gretty would not start on JDK7, when taken from maven (not compiled from sources).

### Version 0.0.18

- implemented [multiple web-apps feature](http://akhikhl.github.io/gretty-doc/Multiple-web-apps-introduction.html).

- implemented [debugger support for multiple web-apps](http://akhikhl.github.io/gretty-doc/Debugging-a-farm.html).

- implemented [integration tests support for multiple web-apps](http://akhikhl.github.io/gretty-doc/Farm-integration-tests.html).

- implemented gretty.afterEvaluate and farm.afterEvaluate closures for easy configuration of gretty tasks.

- implemented highly customizable [gretty task classes](http://akhikhl.github.io/gretty-doc/Gretty-task-classes.html) and [farm task classes](http://akhikhl.github.io/gretty-doc/Farm-task-classes.html).

- improved security of realmConfigFile: now it uses "${webAppDir}/WEB-INF" as a base folder, if you specify relative path.

- completely rewritten documentation.

### Version 0.0.17

- fixed incorrect parameter passing to javaexec in GrettyStartTask, preventing debug mode.

### Version 0.0.16

- Reimplemented Gretty tasks as reusable classes.

- Renamed integrationTestStatusPort to [statusPort](http://akhikhl.github.io/gretty-doc/Gretty-configuration.html#_statusport).

- Moved documentation from README.md to [wiki pages](../../wiki/).

- Updated documentation, added **nice diagrams** to every task description. See more at [wiki pages](../../wiki/).

### Version 0.0.15

- Introduced configuration property [fastReload](http://akhikhl.github.io/gretty-doc/Gretty-configuration.html#_fastreload).

- Fixed JDK-8 compatibility issues.

### Version 0.0.14

- Introduced configuration property [jvmArgs](http://akhikhl.github.io/gretty-doc/Gretty-configuration.html#_jvmargs).

### Version 0.0.13

- Implemented [support of web fragments](http://akhikhl.github.io/gretty-doc/Web-fragments-support.html)
- Implemented integration tests for most of the examples
- Introduced bintray publishing configuration in build.gradle

### Version 0.0.12

- Implemented [support of integration tests](http://akhikhl.github.io/gretty-doc/Integration-tests-support.html)

### Version 0.0.11

- Introduced configuration property [logbackConfigFile](http://akhikhl.github.io/gretty-doc/Gretty-configuration.html#_logbackconfigfile)
  (in response to [issue #6](https://github.com/akhikhl/gretty/issues/6) "Possibility to provide custom logback.xml or logback.groovy configuration")

### Version 0.0.10

- Fixed overlay WAR generation.
- Upgraded to logback version 1.1.1 and slf4j version 1.7.6.
- Updated documentation.

### Version 0.0.9

- Implemented out-of-the-box [JEE annotations support](http://akhikhl.github.io/gretty-doc/JEE-annotations-support.html).
- Various bug-fixes.

### Version 0.0.8

- Implemented support of [jetty.xml](http://akhikhl.github.io/gretty-doc/jetty.xml-support.html) and [jetty-env.xml](http://akhikhl.github.io/gretty-doc/jetty-env.xml-support.html).

### Version 0.0.7

- Implemented accurate re-configuration of logback loggers and appenders on hot-deployment.

### Version 0.0.6

- Implemented support of [multiple jetty versions and multiple servlet API versions](http://akhikhl.github.io/gretty-doc/Switching-between-servlet-containers.html).

### version 0.0.5

- Implemented [debugger support](http://akhikhl.github.io/gretty-doc/Debugger-support.html) and [logging](http://akhikhl.github.io/gretty-doc/Logging.html).

### Version 0.0.4

- Implemented [hot deployment](http://akhikhl.github.io/gretty-doc/Hot-deployment.html).
