![logo](http://akhikhl.github.io/gretty/media/gretty_logo_1.x.png "gretty logo")

[![Build Status](http://img.shields.io/travis/akhikhl/gretty.svg)](https://travis-ci.org/akhikhl/gretty)
[![Maintainer Status](http://stillmaintained.com/akhikhl/gretty.png)](http://stillmaintained.com/akhikhl/gretty) 
[![Release](http://img.shields.io/badge/release-1.1.4-47b31f.svg)](https://github.com/akhikhl/gretty/releases/latest)
[![Snapshot](http://img.shields.io/badge/current-1.1.5--SNAPSHOT-47b31f.svg)](https://github.com/akhikhl/gretty/tree/master)
[![License](http://img.shields.io/badge/license-MIT-47b31f.svg)](#copyright-and-license)

Gretty is a feature-rich gradle plugin for running web-apps on embedded servlet containers.
It supports Jetty versions 7, 8 and 9, Tomcat versions 7 and 8, multiple web-apps and many more.
It wraps servlet container functions as convenient Gradle tasks and configuration DSL.

A complete list of Gretty features is available in [feature overview](http://akhikhl.github.io/gretty-doc/Feature-overview.html).

#### Where to start

If you are new with Gretty, good starting point would be [getting started](http://akhikhl.github.io/gretty-doc/Getting-started.html) page.

#### :star: What's new

- New feature: [inplaceMode property](http://akhikhl.github.io/gretty-doc/Gretty-configuration.html#_inplacemode), when assigned to "hard", instructs Gretty to serve files directly from src/main/webapp, bypassing file copy on change.

- New feature: [runner arguments](http://akhikhl.github.io/gretty-doc/Runner-arguments.html) for Gretty products.

- New feature: [interactiveMode property](http://akhikhl.github.io/gretty-doc/Gretty-configuration.html#_interactivemode) allows to fine-tune Gretty's reaction on keypresses.

- New feature: archiveProduct task, archives the generated product to zip-file.

- New feature: [gretty.springBootVersion property](http://akhikhl.github.io/gretty-doc/Gretty-configuration.html#_springbootversion) allows to specify spring boot version (the default is 1.1.7.RELEASE) (issue #88, "Set Spring / SpringBoot version doesn't work").

- New feature: [gretty.enableNaming property](http://akhikhl.github.io/gretty-doc/Gretty-configuration.html#_enablenaming) allows to enable JNDI naming on Tomcat (issue #64, "JNDI - NoInitialContextException with Tomcat (tried in 7x and 8x)").

- Enhancement: now [gretty.jvmArgs property](http://akhikhl.github.io/gretty-doc/Gretty-configuration.html#_jvmargs) is automatically passed to Gretty products.

- Enhancement in Jetty/Windows-specific lifecycle: useFileMappedBuffer is set to false for all Gretty tasks, so that Jetty does not lock css/js files.

- Enhancement in buildProduct task: now it automatically generates VERSION.txt file with the version and creation date information.

- Resolved issue #89, "How to configure fastReload?".

- Upgrades: 
  - gradle wrapper to version 2.1
  - Groovy to version 2.3.7
  - SpringBoot to version 1.1.7.RELEASE
  - Embedded Tomcat 7 to version 7.0.55
  - Embedded Tomcat 8 to version 8.0.14 
  - Embedded Jetty 9 to version 9.2.3.v20140905
  - asm to version 5.0.3
  
- Implemented support of Gradle 1.10 (still, using Gradle 2.1 is highly recommended!).

- fixed issues with groovy-all versions and logback versions in the webapp classpath

See also: [complete list of changes](changes.md) in this and previous versions.

#### Documentation

You can learn about all Gretty features in [online documentation](http://akhikhl.github.io/gretty-doc/).

#### System requirements

Gretty requires JDK7 or JDK8 and Gradle 1.12 or newer.

Gretty also works on JDK6, although Jetty support is limited to versions 7 and 8 in this case. This is due to the fact that Jetty 9 was compiled against JDK7 and it's bytecode is not compatible with JDK6.

#### Availability

Gretty is an open-source project and is freely available in sources as well as in compiled form.

All releases of Gretty are available at [jcenter](https://bintray.com/akhikhl/maven/gretty/view) and [maven central](http://search.maven.org/#search|ga|1|g%3A%22org.akhikhl.gretty%22) under the group 'org.akhikhl.gretty'.

#### Copyright and License

Copyright 2013-2014 (c) Andrey Hihlovskiy

All versions, present and past, of Gretty are licensed under [MIT license](LICENSE).

[![Project Stats](https://www.ohloh.net/p/gretty/widgets/project_thin_badge.gif)](https://www.ohloh.net/p/gretty)
[![Support via Gittip](https://rawgithub.com/twolfson/gittip-badge/0.2.0/dist/gittip.png)](https://www.gittip.com/akhikhl/)
