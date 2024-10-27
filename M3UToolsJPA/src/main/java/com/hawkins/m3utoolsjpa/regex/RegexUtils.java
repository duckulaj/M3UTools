package com.hawkins.m3utoolsjpa.regex;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RegexUtils {

public static boolean containsRegex(String stringToTest, Pattern pattern) {
		
		Matcher matcher = pattern.matcher(stringToTest);
		if (matcher.matches()) {
			return true;
		}
		return false;
		
	}

	public static String removeCountryIdentifier (String stringToParse, String[] charactersToMatch) {
	
		String regexPattern = String.join("|", charactersToMatch);
		
		if (Pattern.compile("^(" + regexPattern + ")").matcher(stringToParse).find()) {
			stringToParse = stringToParse.substring(2).trim();
		}
		
		return stringToParse;
		
	}
}
