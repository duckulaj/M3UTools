package com.hawkins.m3utoolsjpa.utils;


import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.LoggerContext;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class LoggerUtils {
	
	public static void updateLogLevel(String level) {
		
		LoggerContext loggerContext = (LoggerContext) LoggerFactory.getILoggerFactory();
		
		ch.qos.logback.classic.Logger logger = loggerContext.getLogger(Logger.ROOT_LOGGER_NAME);
		
		switch (level) {
			case "DEBUG" -> logger.setLevel(Level.DEBUG);
			case "WARN" -> logger.setLevel(Level.WARN);
			case "ERROR" -> logger.setLevel(Level.ERROR);
			case "TRACE" -> logger.setLevel(Level.TRACE);
			default -> logger.setLevel(Level.INFO);
		}
		
		log.warn("Logging Level changed to {}", logger.getLevel().levelStr);
	}

	public static List<String> getLogLevelsAsList() {
		List<String> logLevels = new ArrayList<String>();
	    logLevels.add(Level.INFO.levelStr);
	    logLevels.add(Level.WARN.levelStr);
	    logLevels.add(Level.ERROR.levelStr);
	    logLevels.add(Level.DEBUG.levelStr);
	    logLevels.add(Level.TRACE.levelStr);
	    
	    return logLevels;
	}
	
	public static String getCurrentLogLevel() {
		
		LoggerContext loggerContext = (LoggerContext) LoggerFactory.getILoggerFactory();
		
		ch.qos.logback.classic.Logger logger = loggerContext.getLogger(Logger.ROOT_LOGGER_NAME);
		
		return logger.getLevel().levelStr;
	}
}
