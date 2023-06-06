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
}
