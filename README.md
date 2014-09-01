![logo](http://akhikhl.github.io/gretty/media/gretty_logo_1.x.png "gretty logo")

[![Build Status](http://img.shields.io/travis/akhikhl/gretty.svg)](https://travis-ci.org/akhikhl/gretty)
[![Maintainer Status](http://stillmaintained.com/akhikhl/gretty.png)](http://stillmaintained.com/akhikhl/gretty) 
[![Release](http://img.shields.io/badge/release-1.1.1-47b31f.svg)](https://github.com/akhikhl/gretty/releases/latest)
[![Snapshot](http://img.shields.io/badge/current-1.1.2--SNAPSHOT-47b31f.svg)](https://github.com/akhikhl/gretty/tree/master)
[![License](http://img.shields.io/badge/license-MIT-47b31f.svg)](#copyright-and-license)

Gretty is a feature-rich gradle plugin for running web-apps on embedded servlet containers.
It supports Jetty versions 7, 8 and 9, Tomcat versions 7 and 8, multiple web-apps and many more.
It wraps servlet container functions as convenient Gradle tasks and configuration DSL.

A complete list of Gretty features is available in [feature overview](http://akhikhl.github.io/gretty-doc/Feature-overview.html).

#### Where to start

If you are new with Gretty, good starting point would be [getting started](http://akhikhl.github.io/gretty-doc/Getting-started.html) page.

#### :star: What's new

- Gretty 1.1.2-SNAPSHOT new feature: [webapp extra resource bases](http://akhikhl.github.io/gretty-doc/Web-app-extra-resource-bases.html).

- Gretty 1.1.2-SNAPSHOT implements better start/stop protocol, gracefully handling attempts to start Gretty twice (on the same ports). There should be no hanging processes after such attempts anymore.

- Gretty 1.1.2-SNAPSHOT new feature [webapp filtering](http://akhikhl.github.io/gretty-doc/Web-app-filtering.html).

- From now on I'll push snapshot versions of Gretty to jfrog snapshot repository. If you want to use snapshot versions, please add the following to your build script:
  ```groovy
  buildscript {
    repositories {
      // ...
      maven { url 'http://oss.jfrog.org/artifactory/oss-snapshot-local' }
    }
    dependencies {
      classpath 'org.akhikhl.gretty:gretty:1.1.2-SNAPSHOT'
    }
  }
  
  apply plugin: 'org.akhikhl.gretty'
  
  repositories {
    // ...
    maven { url 'http://oss.jfrog.org/artifactory/oss-snapshot-local' }
  }
  ```

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
