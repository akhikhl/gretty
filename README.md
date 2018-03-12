![logo](https://akhikhl.github.io/gretty/media/gretty_logo_1.x.png "gretty logo")

[![Build Status](https://travis-ci.org/gretty-gradle-plugin/gretty.svg?branch=master)](https://travis-ci.org/gretty-gradle-plugin/gretty)
![Maintenance Status](https://img.shields.io/maintenance/yes/2018.svg)
[![Latest release](https://img.shields.io/badge/release-2.0.0-47b31f.svg)](https://github.com/gretty-gradle-plugin/gretty/tree/v2.0.0)
[![Snapshot](https://img.shields.io/badge/current-2.0.1--SNAPSHOT-47b31f.svg)](https://github.com/gretty-gradle-plugin/gretty/tree/master)
[![License](https://img.shields.io/badge/license-MIT-47b31f.svg)](#copyright-and-license)

Gretty is a feature-rich gradle plugin for running web-apps on embedded servlet containers.
It supports Jetty versions 7, 8 and 9, Tomcat versions 7 and 8, multiple web-apps and many more.
It wraps servlet container functions as convenient Gradle tasks and configuration DSL.

A complete list of Gretty features is available in [feature overview](https://gretty-gradle-plugin.github.io/gretty-doc/Feature-overview.html).

#### Where to start

[![Join the chat at https://gitter.im/saladinkzn/gretty](https://badges.gitter.im/saladinkzn/gretty.svg)](https://gitter.im/saladinkzn/gretty?utm_source=badge&utm_medium=badge&utm_campaign=pr-badge&utm_content=badge)

If you are new with Gretty, try [getting started](https://gretty-gradle-plugin.github.io/gretty-doc/Getting-started.html) page.

#### :star: What's new

March 12, 2018, Gretty(.org) 2.1.0 is out and immediately available at [Bintray](https://bintray.com/javabrett/maven/org.gretty/view).

This release is a fork of the original project by Andrey Hihlovskiy (@akhikhl) hosted at https://github.com/akhikhl/gretty .  The fork is to encourage additional maintenance and development contributions to the project.  The main change is that the plugin-group simply becomes `org.gretty`.

Gradle/Jetty/Tomcat/Gretty users and developers are eternally grateful for Andrey's contribution of the Gretty codebase and look forward to his future contributions and guidance.

* Changes in this version:

* Project fork - plugin group is now org.gretty.

* Compatibility with Gradle 4.6 (with thanks to Stefan Wolf)

* Compatability with JDK9. Note that some latest container versions do not have full JDK9 compatibility at the time of release.

* Tomcat 9 support (with thanks to Boris Petrov).

* Upgraded default Jetty 9.4 to latest.

* Updated SpringLoaded version, fixed #408.

* Updated ASM version.

* Updated Groovy version.

* Gretty no longer adds org.slf4j:slf4j-nop:1.7.12 if SLF4J impl is missing, fixed #394.

See also: [complete list of changes](changes.md) for more information.

#### Documentation

You can learn about all Gretty features in [online documentation](https://gretty-gradle-plugin.github.io/gretty-doc/).

#### System requirements

Gretty requires JDK7 or JDK8 and Gradle 1.10 or newer (Gradle 4.0 is highly recommended!).

Since version 2.0.0 Gretty no longer supports JDK6.

#### Availability

Gretty is an open-source project and is freely available in sources as well as in compiled form.

All releases of Gretty are available at [Bintray](https://bintray.com/akhikhl/maven/gretty/view)

#### Copyright and License

Copyright 2013-2018 (c) Andrey Hihlovskiy, Timur Shakurov and [contributors](CONTRIBUTORS).

All versions, present and past, of Gretty are licensed under [MIT license](LICENSE).
