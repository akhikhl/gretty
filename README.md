![logo](http://akhikhl.github.io/gretty/media/gretty_logo_1.x.png "gretty logo")

[![Build Status](https://travis-ci.org/akhikhl/gretty.png?branch=master)](https://travis-ci.org/akhikhl/gretty) 
[![Maintainer Status](http://stillmaintained.com/akhikhl/gretty.png)](http://stillmaintained.com/akhikhl/gretty) 
[![Latest Version](http://img.shields.io/badge/latest_version-1.1.0-47b31f.svg)](https://github.com/akhikhl/gretty/tree/v1.1.0)
[![License](http://img.shields.io/badge/license-MIT-949494.svg)](#copyright-and-license)

Gretty is a feature-rich gradle plugin for running web-apps on embedded servlet containers.
It supports Jetty versions 7, 8 and 9, Tomcat versions 7 and 8, multiple web-apps and many more.
It wraps servlet container functions as convenient Gradle tasks and configuration DSL.

A complete list of Gretty features is available in [feature overview](http://akhikhl.github.io/gretty-doc/Feature-overview.html).

#### Where to start

If you are new with Gretty, good starting point would be [getting started](http://akhikhl.github.io/gretty-doc/Getting-started.html) page.

#### :star: What's new

- [Generation of self-contained runnable products](http://akhikhl.github.io/gretty-doc/Product-generation.html).

See also: [complete list of changes](changes.md) in this and previous versions.

#### Documentation

You can learn about all Gretty features in [online documentation](http://akhikhl.github.io/gretty-doc/).

#### System requirements

Gretty requires JDK7 or JDK8 and Gradle 1.12 or newer.

Gretty also works on JDK6, although Jetty support is limited to versions 7 and 8 in this case. This is due to the fact that Jetty 9 was compiled against JDK7 and it's bytecode is not compatible with JDK6.

#### Availability

Gretty is an open-source project and is freely available in sources as well as in compiled form.

All versions of gretty are available at [jcenter](https://bintray.com/akhikhl/maven/gretty/view) and [maven central](http://search.maven.org/#search|ga|1|g%3A%22org.akhikhl.gretty%22) under the group 'org.akhikhl.gretty'.

#### Copyright and License

Copyright 2013-2014 (c) Andrey Hihlovskiy

All versions, present and past, of Gretty are licensed under [MIT license](license.txt).

[![Project Stats](https://www.ohloh.net/p/gretty/widgets/project_thin_badge.gif)](https://www.ohloh.net/p/gretty)
[![Support via Gittip](https://rawgithub.com/twolfson/gittip-badge/0.2.0/dist/gittip.png)](https://www.gittip.com/akhikhl/)
