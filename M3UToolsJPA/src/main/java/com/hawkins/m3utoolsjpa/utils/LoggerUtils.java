
package com.hawkins.m3utoolsjpa.utils;

import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.LoggerContext;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class LoggerUtils {

    private static final Map<String, Level> LEVEL_MAP = Map.of(
        "DEBUG", Level.DEBUG,
        "WARN", Level.WARN,
        "ERROR", Level.ERROR,
        "TRACE", Level.TRACE,
        "INFO", Level.INFO
    );

    public static void updateLogLevel(String level) {
        LoggerContext loggerContext = (LoggerContext) LoggerFactory.getILoggerFactory();
        ch.qos.logback.classic.Logger logger = loggerContext.getLogger(Logger.ROOT_LOGGER_NAME);
        logger.setLevel(LEVEL_MAP.getOrDefault(level, Level.INFO));
        log.warn("Logging Level changed to {}", logger.getLevel().levelStr);
    }

    public static List<String> getLogLevelsAsList() {
        return List.of(
            Level.INFO.levelStr,
            Level.WARN.levelStr,
            Level.ERROR.levelStr,
            Level.DEBUG.levelStr,
            Level.TRACE.levelStr
        );
    }

    public static String getCurrentLogLevel() {
        LoggerContext loggerContext = (LoggerContext) LoggerFactory.getILoggerFactory();
        ch.qos.logback.classic.Logger logger = loggerContext.getLogger(Logger.ROOT_LOGGER_NAME);
        return logger.getLevel().levelStr;
    }
}
