## Gretty 1.2.3 release announcement

May 18, 2015: Gretty version 1.2.3 is out and immediately available at github, bintray, maven central and gradle plugin portal!

Gretty version 1.2.3 introduces the following changes:

1. Resolved issue #175 "SpringBoot applications are not isolated from one another in farm and product runs"

2. Resolved issues with logging: #164, #145, #133

3. Upgraded to Tomcat 7.0.61 and Tomcat 8.0.22

4. Upgrade and tests on Gradle 2.4 (Gradle versions >= 1.10 are still supported)

## Gretty 1.2.2 release announcement

Apr 20, 2015: Gretty version 1.2.2 is out and immediately available at github, bintray, maven central and gradle plugin portal!

This is a maintenance release, fixing a single [issue #163](https://github.com/akhikhl/gretty/issues/152): MissingPropertyException when running Tomcat with HTTPS.

## Gretty 1.2.1 release announcement

Apr 11, 2015: Gretty version 1.2.1 is out and immediately available at github, bintray, maven central and gradle plugin portal!

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

## Gretty 1.2.0 release announcement

Mar 6, 2015: Gretty version 1.2.0 is out and immediately available at github, bintray, maven central and gradle plugin portal!

Gretty version 1.2.0 introduces the following changes:

* Upgrade to Jetty 9.2.9.v20150224, which fixes [critical vulnerability CVE-2015-2080](https://github.com/eclipse/jetty.project/blob/master/advisories/2015-02-24-httpparser-error-buffer-bleed.md)
* Fix for class reloading with WAR tasks. Now if some class is changed (recompiled), the web-application restarts as expected.
* Fix for the bug: slf4j/logback libraries are excluded from webapps packed into Gretty product.

## Gretty 1.1.9 release announcement

Mar 3, 2015: Gretty version 1.1.9 is out and immediately available at github, bintray, maven central and gradle plugin portal!

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

## Gretty 1.1.8 release announcement

Dec 8, 2014: Gretty version 1.1.8 is out and immediately available at github, bintray, maven central and gradle plugin portal!

Gretty version 1.1.8 introduces new parameters for [fine-tuning of debugging tasks](http://akhikhl.github.io/gretty-doc/Debugger-support.html#_fine_tuning).

Also Gretty version 1.1.8 includes upgrades of the following components to their latest versions:

- Geb : 0.9.3 -> 0.10.0
- gradle-bintray-plugin : 0.4 -> 1.0
- Gradle Wrapper : 2.1 -> 2.1.1
- Groovy : 2.3.7 -> 2.3.8
- Tomcat-8 : 8.0.14 -> 8.0.15
- Selenium : 2.43.1 -> 2.44.0
- Spring Boot : 1.1.8.RELEASE -> 1.1.9.RELEASE

## Gretty 1.1.7 release announcement

Gretty version 1.1.7 is out and immediately available at github, bintray, maven central and gradle plugin portal!

Gretty version 1.1.7 introduces new feature: [redirect filter](http://akhikhl.github.io/gretty-doc/Redirect-filter.html). 
The feature is completely independent from the rest of Gretty and can be deployed as part of WAR-file. 
Charming thing about redirect filter is that it provides groovy-based configuration DSL.

## Gretty 1.1.6 release announcement

Gretty version 1.1.5 brings new bug, preventing Gretty product generation.
If you run buildProduct task and experience "readonly property" exception,
please switch to Gretty 1.1.6 - it fixes the problem.

## Gretty 1.1.5 release announcement

Gretty version 1.1.5 is out and immediately available at github, bintray, maven central and gradle plugin portal!

Gretty is a feature-rich gradle plugin for running webapps on Jetty and Tomcat. Sources are available here:
https://github.com/akhikhl/gretty

This release brings better integration with Spring Boot, better interpretation of "jetty.xml" file and other improvements.

What's new in Gretty 1.1.5:

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

Here is [full list of changes](https://github.com/akhikhl/gretty/blob/master/changes.md) in this and previous releases.

I would be thankful for your feedback, bug reports and suggestions!
