package com.hawkins.m3utoolsjpa.utils;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.LoggerContext;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class LoggerUtils {
	
	public static void updateLogLevel(String level) {
		
		Logger rootLogger = (Logger) LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);
	
		LoggerContext loggerContext = (LoggerContext) LoggerFactory.getILoggerFactory();
		
		ch.qos.logback.classic.Logger logger = loggerContext.getLogger(Logger.ROOT_LOGGER_NAME);
		
		switch (level) {
		case "debug":
			logger.setLevel(Level.DEBUG);
			break;
		default:
			logger.setLevel(Level.INFO);
		}
		
		
        
	}

}
