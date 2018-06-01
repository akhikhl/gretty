/*
 * Gretty
 *
 * Copyright (C) 2013-2015 Andrey Hihlovskiy and contributors.
 *
 * See the file "LICENSE" for copying and usage permission.
 * See the file "CONTRIBUTORS" for complete list of contributors.
 */
scan '30 seconds'

def appenders = []

def logDir_ = logDir
def logFileName_ = logFileName

if(consoleLogEnabled) {
  appenders.add('CONSOLE')
  appender('CONSOLE', ConsoleAppender) {
    encoder(PatternLayoutEncoder) {
      pattern = '%-8date{HH:mm:ss} %-5level %msg%n'
    }
  }
}

if(fileLogEnabled) {
  appenders.add('FILE')
  appender('FILE', RollingFileAppender) {
    file = "${logDir_}/${logFileName_}.log"
    append = true
    rollingPolicy(TimeBasedRollingPolicy) {
      fileNamePattern = "${logDir_}/${logFileName_}-%d{yyyy-MM-dd_HH}.log"
      maxHistory = 7
    }
    encoder(PatternLayoutEncoder) {
      pattern = '%-8date{HH:mm:ss} %-5level %logger{35} - %msg%n'
    }
  }
}

root loggingLevel, appenders

logger 'org.apache.catalina', WARN

logger 'org.apache.coyote', WARN

logger 'org.apache.jasper', WARN

logger 'org.apache.tomcat', WARN

logger 'org.eclipse.jetty', WARN

logger 'org.eclipse.jetty.annotations.AnnotationConfiguration', ERROR

logger 'org.eclipse.jetty.annotations.AnnotationParser', ERROR

logger 'org.eclipse.jetty.util.component.AbstractLifeCycle', ERROR
