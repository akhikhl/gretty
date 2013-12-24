/*
 * gretty
 *
 * Copyright 2013  Andrey Hihlovskiy.
 *
 * See the file "license.txt" for copying and usage permission.
 */
package org.akhikhl.gretty

import ch.qos.logback.classic.Level
import ch.qos.logback.classic.Logger
import ch.qos.logback.classic.LoggerContext
import ch.qos.logback.classic.encoder.PatternLayoutEncoder
import ch.qos.logback.classic.turbo.ReconfigureOnChangeFilter
import ch.qos.logback.core.ConsoleAppender
import ch.qos.logback.core.rolling.RollingFileAppender
import ch.qos.logback.core.rolling.TimeBasedRollingPolicy

import org.slf4j.LoggerFactory

final class LoggingUtils {

  static void configureLogging(Map options) {

    Level loggingLevel = Level.toLevel(options.loggingLevel)
    boolean consoleLogEnabled = options.consoleLogEnabled as boolean
    boolean fileLogEnabled = options.fileLogEnabled as boolean
    String logFileName = options.logFileName
    String logDir = options.logDir

    LoggerContext logCtx = LoggerFactory.getILoggerFactory()

    logCtx.addTurboFilter(new ReconfigureOnChangeFilter().with {
      context = logCtx
      refreshPeriod = 3 * 1000 // milliseconds
      start()
      it
    })

    def logConsoleAppender
    if(consoleLogEnabled && loggingLevel != Level.OFF)
      logConsoleAppender = new ConsoleAppender().with {
        context = logCtx
        name = 'console'
        encoder = new PatternLayoutEncoder().with {
          context = logCtx
          pattern = '%-12date{YYYY-MM-dd HH:mm:ss.SSS} %-5level - %msg%n'
          start()
          it
        }
        start()
        it
      }

    def logFileAppender
    if(fileLogEnabled && loggingLevel != Level.OFF) {
      logFileAppender = new RollingFileAppender()
      logFileAppender.with {
        context = logCtx
        name = 'logFile'
        encoder = new PatternLayoutEncoder().with {
          context = logCtx
          pattern = '%-12date{YYYY-MM-dd HH:mm:ss.SSS} %-5level - %msg%n'
          start()
          it
        }
        append = true
        file = "${logDir}/${logFileName}.log"
        rollingPolicy = new TimeBasedRollingPolicy().with {
          context = logCtx
          parent = logFileAppender
          fileNamePattern = "${logDir}/${logFileName}-%d{yyyy-MM-dd_HH}.log"
          maxHistory = 7
          start()
          it
        }
        start()
      }
    }

    logCtx.getLogger('root').with {
      additive = false
      level = loggingLevel
      if(logConsoleAppender)
        addAppender(logConsoleAppender)
      if(logFileAppender)
        addAppender(logFileAppender)
    }
  }
}

