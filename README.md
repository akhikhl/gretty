![logo](http://akhikhl.github.io/gretty/media/gretty_logo_1.x.png "gretty logo")

[![Build Status](http://img.shields.io/travis/akhikhl/gretty.svg)](https://travis-ci.org/akhikhl/gretty)
![Maintenance Status](http://img.shields.io/maintenance/yes/2016.svg)
[![Latest release](http://img.shields.io/badge/release-1.4.0-47b31f.svg)](https://github.com/akhikhl/gretty/tags/v1.4.0)
[![Snapshot](http://img.shields.io/badge/current-1.4.1--SNAPSHOT-47b31f.svg)](https://github.com/akhikhl/gretty/tree/master)
[![License](http://img.shields.io/badge/license-MIT-47b31f.svg)](#copyright-and-license)

Gretty is a feature-rich gradle plugin for running web-apps on embedded servlet containers.
It supports Jetty versions 7, 8 and 9, Tomcat versions 7 and 8, multiple web-apps and many more.
It wraps servlet container functions as convenient Gradle tasks and configuration DSL.

A complete list of Gretty features is available in [feature overview](http://akhikhl.github.io/gretty-doc/Feature-overview.html).

#### Where to start

[![Join the chat at https://gitter.im/saladinkzn/gretty](https://badges.gitter.im/saladinkzn/gretty.svg)](https://gitter.im/saladinkzn/gretty?utm_source=badge&utm_medium=badge&utm_campaign=pr-badge&utm_content=badge)

If you are new with Gretty, try [getting started](http://akhikhl.github.io/gretty-doc/Getting-started.html) page.

#### :star: What's new

Aug 5, 2016: Andrey Hihlovskiy is back to maintaining Gretty! Many thanks to Timur Shakurov for his great work on Gretty maintenance
during the past year!

Aug 5, 2016: Gretty 1.4.0 is out and immediately available at [Bintray](https://bintray.com/akhikhl/maven/gretty/view) and [Maven Central](http://search.maven.org/#search|ga|1|g%3A%22org.akhikhl.gretty%22).

See [Gretty 1.4.0 release announcement](RELEASE.md#gretty-140-release-announcement) for more information.

See also: [complete list of changes](changes.md) in this and previous versions.

#### Documentation

You can learn about all Gretty features in [online documentation](http://akhikhl.github.io/gretty-doc/).

#### System requirements

Gretty requires JDK7 or JDK8 and Gradle 1.10 or newer (Gradle 2.14.1 is highly recommended!).

Gretty also works on JDK6, although Jetty support is limited to versions 7 and 8 in this case. This is due to the fact that Jetty 9 was compiled against JDK7 and it's bytecode is not compatible with JDK6.

#### Availability

Gretty is an open-source project and is freely available in sources as well as in compiled form.

All releases of Gretty are available at [Bintray](https://bintray.com/akhikhl/maven/gretty/view)

#### Copyright and License

Copyright 2013-2016 (c) Andrey Hihlovskiy, Timur Shakurov and [contributors](CONTRIBUTORS).

All versions, present and past, of Gretty are licensed under [MIT license](LICENSE).
