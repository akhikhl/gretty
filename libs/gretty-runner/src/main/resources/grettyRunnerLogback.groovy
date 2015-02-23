/*
 * Gretty
 *
 * Copyright (C) 2013-2014 Andrey Hihlovskiy and contributors.
 *
 * See the file "LICENSE" for copying and usage permission.
 * See the file "CONTRIBUTORS" for complete list of contributors.
 */
scan '30 seconds'

String logDir = "${System.getProperty('user.home')}/logs"
String logFileName = 'gretty'

appender('CONSOLE', ConsoleAppender) {
  encoder(PatternLayoutEncoder) {
    pattern = '%-8date{HH:mm:ss} %-5level %msg%n'
  }
}

appender('FILE', RollingFileAppender) {
  file = "${logDir}/${logFileName}.log"
  append = true
  rollingPolicy(TimeBasedRollingPolicy) {
    fileNamePattern = "${logDir}/${logFileName}-%d{yyyy-MM-dd_HH}.log"
    maxHistory = 7
  }
  encoder(PatternLayoutEncoder) {
    pattern = '%-8date{HH:mm:ss} %-5level %logger{35} - %msg%n'
  }
}

root INFO, ['CONSOLE', 'FILE']

logger 'org.apache.catalina', WARN

logger 'org.apache.coyote', WARN

logger 'org.apache.jasper', WARN

logger 'org.apache.tomcat', WARN

logger 'org.eclipse.jetty', WARN

logger 'org.eclipse.jetty.annotations.AnnotationConfiguration', ERROR