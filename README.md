![logo](https://akhikhl.github.io/gretty/media/gretty_logo_1.x.png "gretty logo")

[![Build Status](https://travis-ci.org/gretty-gradle-plugin/gretty.svg?branch=master)](https://travis-ci.org/gretty-gradle-plugin/gretty)
![Maintenance Status](https://img.shields.io/maintenance/yes/2018.svg)
[![Latest release](https://img.shields.io/badge/release-2.3.0-47b31f.svg)](https://github.com/gretty-gradle-plugin/gretty/tree/v2.3.0)
[![Snapshot](https://img.shields.io/badge/current-2.4.0--SNAPSHOT-47b31f.svg)](https://github.com/gretty-gradle-plugin/gretty/tree/master)
[![License](https://img.shields.io/badge/license-MIT-47b31f.svg)](#copyright-and-license)

Gretty is a feature-rich gradle plugin for running web-apps on embedded servlet containers.
It supports Jetty versions 7, 8 and 9, Tomcat versions 7, 8 and 9, multiple web-apps and many more.
It wraps servlet container functions as convenient Gradle tasks and configuration DSL.

A complete list of Gretty features is available in [feature overview](https://gretty-gradle-plugin.github.io/gretty-doc/Feature-overview.html).

#### Where to start

[![Join the chat at https://gitter.im/saladinkzn/gretty](https://badges.gitter.im/saladinkzn/gretty.svg)](https://gitter.im/saladinkzn/gretty?utm_source=badge&utm_medium=badge&utm_campaign=pr-badge&utm_content=badge)

If you are new with Gretty, try [getting started](https://gretty-gradle-plugin.github.io/gretty-doc/Getting-started.html) page.

#### :star: What's new

November 28, 2018, Gretty 2.3.0 is out and available at [Gradle Plugins](https://plugins.gradle.org/plugin/org.gretty) and [Bintray](https://bintray.com/javabrett/maven/org.gretty/view).

This release adds support for Gradle 5.0, which was released this week!  Please raise an issue if you find any issues running Gretty 2.3+ with Gradle 5.0.

See also: [complete list of changes](changes.md) for more information.

May 21, 2018, Gretty(.org) 2.2.0 is out and immediately available at [Gradle Plugins](https://plugins.gradle.org/plugin/org.gretty) and [Bintray](https://bintray.com/javabrett/maven/org.gretty/view).

* Changes in this version:

* Bumped default Tomcat 9 version to 9.0.6 (was 9.0.5).

* Support added for Tomcat 8.5 and Tomcat support refactoring (thanks Boris Petrov). Tomcat 8.5 replaces deprecated Tomcat 8.0.

* Bumped Spring Boot version to 1.5.9 (was 1.5.4).

* Bumped versions of asm (6.1.1, was 6.0), Groovy (2.4.15, was 2.4.13) and Spring (4.3.16, was 4.3.9) (thanks Henrik Brautaset Aronsen).

* Fixed incompatibility with java-library plugin (thanks Ollie Freeman).
 
* Dev: various build and test improvements.

See also: [complete list of changes](changes.md) for more information.

#### Documentation

You can learn about all Gretty features in [online documentation](https://gretty-gradle-plugin.github.io/gretty-doc/).

#### System requirements

Gretty requires JDK7, JDK8 or JDK9 and Gradle 1.10 or newer (Gradle 4.0 is highly recommended!).

Since version 2.0.0 Gretty no longer supports JDK6.

#### Availability

Gretty is an open-source project and is freely available in sources as well as in compiled form.

Releases of Gretty (gretty.org fork) from 2.1.0 onwards are available at [Bintray](https://bintray.com/javabrett/maven/org.gretty/view)
Old releases of Gretty up to and including version 2.0.0 are available at [Bintray](https://bintray.com/akhikhl/maven/gretty/view)

#### Copyright and License

Copyright 2013-2018 (c) Andrey Hihlovskiy, Timur Shakurov and [contributors](CONTRIBUTORS).

All versions, present and past, of Gretty are licensed under [MIT license](LICENSE).
