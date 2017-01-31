/*
 * Gretty
 *
 * Copyright (C) 2013-2015 Andrey Hihlovskiy and contributors.
 *
 * See the file "LICENSE" for copying and usage permission.
 * See the file "CONTRIBUTORS" for complete list of contributors.
 */
scan '30 seconds'

String encoderPattern = '%-12date{HH:mm:ss} %-5level %logger{35} - %msg%n'
String logDir = "${System.getProperty('user.home')}/logs"
String logFileName = 'websocket'

appender('CONSOLE', ConsoleAppender) {
  encoder(PatternLayoutEncoder) {
    pattern = encoderPattern
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
    pattern = encoderPattern
  }
}

root WARN, ['CONSOLE', 'FILE']

logger 'org.akhikhl.gretty', INFO

