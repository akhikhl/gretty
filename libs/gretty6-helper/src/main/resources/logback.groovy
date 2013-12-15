import ch.qos.logback.classic.Level

scan '30 seconds'

Level loggingLevel = Level.toLevel(System.getProperty('loggingLevel'))
boolean consoleLogEnabled = System.getProperty('consoleLogEnabled') as boolean
boolean fileLogEnabled = System.getProperty('fileLogEnabled') as boolean
String logFileName = System.getProperty('logFileName')
String logDir = System.getProperty('logDir') ?: "${System.getProperty('user.home')}/logs"

List appenders = []

if(consoleLogEnabled && loggingLevel != OFF) {
  appender('CONSOLE', ConsoleAppender) {
    encoder(PatternLayoutEncoder) {
      pattern = '%-12date{HH:mm:ss} %-5level %logger{35} - %msg%n'
    }
  }
  appenders.add 'CONSOLE'
}

if(fileLogEnabled && loggingLevel != OFF) {
  appender('FILE', RollingFileAppender) {
    file = "${logDir}/${logFileName}.log"
    append = true
    rollingPolicy(TimeBasedRollingPolicy) {
      fileNamePattern = "${logDir}/${logFileName}-%d{yyyy-MM-dd_HH}.log"
      maxHistory = 7
    }
    encoder(PatternLayoutEncoder) {
      pattern = '%-12date{HH:mm:ss.SSS} %-5level %logger{35} - %msg%n'
    }
  }
  appenders.add 'FILE'
}

if(appenders)
  root loggingLevel, appenders
