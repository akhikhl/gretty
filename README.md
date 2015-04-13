![logo](http://akhikhl.github.io/gretty/media/gretty_logo_1.x.png "gretty logo")

[![Build Status](http://img.shields.io/travis/akhikhl/gretty.svg)](https://travis-ci.org/akhikhl/gretty)
[![Maintainer Status](http://stillmaintained.com/akhikhl/gretty.png)](http://stillmaintained.com/akhikhl/gretty) 
[![Release](http://img.shields.io/badge/release-1.2.1-47b31f.svg)](https://github.com/akhikhl/gretty/releases/latest)
[![Snapshot](http://img.shields.io/badge/current-1.2.2--SNAPSHOT-47b31f.svg)](https://github.com/akhikhl/gretty/tree/master)
[![License](http://img.shields.io/badge/license-MIT-47b31f.svg)](#copyright-and-license)

Gretty is a feature-rich gradle plugin for running web-apps on embedded servlet containers.
It supports Jetty versions 7, 8 and 9, Tomcat versions 7 and 8, multiple web-apps and many more.
It wraps servlet container functions as convenient Gradle tasks and configuration DSL.

A complete list of Gretty features is available in [feature overview](http://akhikhl.github.io/gretty-doc/Feature-overview.html).

#### Where to start

If you are new with Gretty, try [getting started](http://akhikhl.github.io/gretty-doc/Getting-started.html) page.

#### :star: What's new

Apr 11, 2015: Gretty 1.2.1 is out and immediately available at [Gradle plugin portal](http://plugins.gradle.org/plugin/org.akhikhl.gretty), 
[Bintray](https://bintray.com/akhikhl/maven/gretty/view) and [Maven Central](http://search.maven.org/#search|ga|1|g%3A%22org.akhikhl.gretty%22).

See [Gretty 1.2.1 release announcement](RELEASE.md#gretty-121-release-announcement) for more information.

See also: [complete list of changes](changes.md) in this and previous versions.

#### Documentation

You can learn about all Gretty features in [online documentation](http://akhikhl.github.io/gretty-doc/).

#### System requirements

Gretty requires JDK7 or JDK8 and Gradle 1.10 or newer (Gradle 2.3 is highly recommended!).

Gretty also works on JDK6, although Jetty support is limited to versions 7 and 8 in this case. This is due to the fact that Jetty 9 was compiled against JDK7 and it's bytecode is not compatible with JDK6.

#### Availability

Gretty is an open-source project and is freely available in sources as well as in compiled form.

All releases of Gretty are available at [Bintray](https://bintray.com/akhikhl/maven/gretty/view) and [Maven Central](http://search.maven.org/#search|ga|1|g%3A%22org.akhikhl.gretty%22) under the group 'org.akhikhl.gretty'.

#### Copyright and License

Copyright 2013-2015 (c) Andrey Hihlovskiy and [contributors](CONTRIBUTORS).

All versions, present and past, of Gretty are licensed under [MIT license](LICENSE).

[![Project Stats](https://www.ohloh.net/p/gretty/widgets/project_thin_badge.gif)](https://www.ohloh.net/p/gretty)
[![Support via Gittip](https://rawgithub.com/twolfson/gittip-badge/0.2.0/dist/gittip.png)](https://www.gittip.com/akhikhl/)
