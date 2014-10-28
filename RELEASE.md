== Gretty 1.1.5 release announcement

Hello colleagues,

Gretty version 1.1.5 is out and immediately available at github, bintray, maven central and gradle plugin portal!

Gretty is a feature-rich gradle plugin for running webapps on Jetty and Tomcat. Sources are available here:
https://github.com/akhikhl/gretty

What's new in Gretty 1.1.5:

- New feature: composite farms.
- New feature dependent projects can run in inplace mode.
- New feature: override of context path in integration test tasks.
- New feature: injection of version variables into project.ext.
- Upgraded to Spring Boot 1.1.8.RELEASE.
- Fixed bug: spring-boot improperly shutdown in SpringBootServerManager.stopServer.
- Resolved issue #101: Jetty.xml Rewrite Handler doesnt seem to take effect.
- Resolved issue #97: How can I add runner libraries.
- Resolved issue #96: Custom builds of Gradle cause NumberFormatException.
- Resolved issue #93: Groovy version conflicts when running farmStart with a war file.

Full list of changes: https://github.com/akhikhl/gretty/blob/master/changes.md

I would be thankful for your feedback, bug reports and suggestions!
