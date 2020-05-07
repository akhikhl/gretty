package org.akhikhl.gretty

import ch.qos.logback.classic.Level
import ch.qos.logback.classic.Logger
import org.slf4j.LoggerFactory

class LogUtil {

    static setLevel(boolean debugEnabled) {
        def logger = LoggerFactory.getLogger(LogUtil.class.getPackage().getName())
        if (logger instanceof Logger) {
            logger.level = debugEnabled ? Level.DEBUG : Level.INFO
        }
    }
}
