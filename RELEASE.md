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
