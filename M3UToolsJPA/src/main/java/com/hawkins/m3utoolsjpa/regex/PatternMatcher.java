package com.hawkins.m3utoolsjpa.regex;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PatternMatcher implements Runnable {

	private static PatternMatcher thisInstance = null;
	private static Matcher matcher = null;
	
	public PatternMatcher( ) {

		Pattern defaultPattern = Pattern.compile("");
		this.matcher = defaultPattern.matcher("");
		
	}
	
	public static synchronized PatternMatcher getInstance()
	{
		if (PatternMatcher.thisInstance == null)
		{
			PatternMatcher.thisInstance = new PatternMatcher();
		}

		return PatternMatcher.thisInstance;
	}
	
	public String extract(String line, Pattern pattern) {
		
		matcher.reset();
		matcher = pattern.matcher(line);
		if (matcher.matches()) {
			return matcher.group(1);
		}
		return null;
	}
		
	@Override
	public void run() {
		throw new UnsupportedOperationException();
	}
}
