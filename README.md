![logo](http://akhikhl.github.io/gretty/media/gretty_logo_1.x.png "gretty logo")

[![Build Status](http://img.shields.io/travis/saladinkzn/gretty.svg)](https://travis-ci.org/saladinkzn/gretty)
[![Snapshot](http://img.shields.io/badge/current-1.2.5--SNAPSHOT-47b31f.svg)](https://github.com/saladinkzn/gretty/tree/master)
[![License](http://img.shields.io/badge/license-MIT-47b31f.svg)](#copyright-and-license)

Gretty is a feature-rich gradle plugin for running web-apps on embedded servlet containers.
It supports Jetty versions 7, 8 and 9, Tomcat versions 7 and 8, multiple web-apps and many more.
It wraps servlet container functions as convenient Gradle tasks and configuration DSL.

A complete list of Gretty features is available in [feature overview](http://akhikhl.github.io/gretty-doc/Feature-overview.html).

#### Where to start

If you are new with Gretty, try [getting started](http://akhikhl.github.io/gretty-doc/Getting-started.html) page.

Fork-specific information here: [wiki](https://github.com/saladinkzn/gretty/wiki)

#### :star: What's new

June 2, 2015: Gretty 1.2.4 is out and immediately available at [Gradle plugin portal](http://plugins.gradle.org/plugin/org.akhikhl.gretty), 
[Bintray](https://bintray.com/saladinkzn/maven/gretty-fork/view) and [Maven Central](http://search.maven.org/#search|ga|1|g%3A%22org.akhikhl.gretty%22).

See [Gretty 1.2.4 release announcement](RELEASE.md#gretty-124-release-announcement) for more information.

See also: [complete list of changes](changes.md) in this and previous versions.

#### Documentation

You can learn about all Gretty (pre-fork) features in [online documentation](http://akhikhl.github.io/gretty-doc/).
Docs for new features will be available soon.

#### System requirements

Gretty requires JDK7 or JDK8 and Gradle 1.10 or newer (Gradle 2.4 is highly recommended!).

Gretty also works on JDK6, although Jetty support is limited to versions 7 and 8 in this case. This is due to the fact that Jetty 9 was compiled against JDK7 and it's bytecode is not compatible with JDK6.

#### Availability

Gretty is an open-source project and is freely available in sources as well as in compiled form.

All releases of Gretty are available at [Bintray](https://bintray.com/saladinkzn/maven/gretty-fork/view)

#### Copyright and License

Copyright 2013-2015 (c) Timur Shakurov, Andrey Hihlovskiy and [contributors](CONTRIBUTORS).

All versions, present and past, of Gretty are licensed under [MIT license](LICENSE).
