package com.hawkins.m3utoolsjpa.utils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.hawkins.m3utoolsjpa.regex.Patterns;

public class StringUtils {

	private StringUtils() {
		throw new IllegalStateException("Utility class");
	}

	public static boolean isNullOrEmpty(String str) {
		return str == null || str.length() < 1;
	}

	public static boolean isNullOrEmptyOrBlank(String str) {
		return str == null || str.trim().length() < 1;
	}

	public static byte[] getBytes(StringBuffer sb) {
		return sb.toString().getBytes();
	}

	public static byte[] getBytes(StringBuilder sb) {
		return sb.toString().getBytes();
	}

	public static byte[] getBytes(String s) {
		return s.getBytes();
	}

	public static String removeCountryIdentifier(String toParse) {

		Matcher matcher = Patterns.STRIP_COUNTRY_IDENTIFIER.matcher(toParse);

		if(matcher.find()) {
			toParse = matcher.replaceFirst("");
		}

		return toParse;

	}
	
	public static String removeCountryIdentifierUsingRegExpr(String toParse, String regExpr) {

		Matcher matcher = Pattern.compile(regExpr).matcher(toParse);

		if(matcher.find()) {
			toParse = matcher.replaceFirst("");
		}

		return toParse;

	}

	public static String cleanTextContent(String text) {
		// strips off all non-ASCII characters
		text = text.replaceAll("[^\\x00-\\x7F]", "");

		// erases all the ASCII control characters
		text = text.replaceAll("[\\p{Cntrl}&&[^\r\n\t]]", "");

		// removes non-printable characters from Unicode
		text = text.replaceAll("\\p{C}", "");

		return text.trim();
	}
	
	public static String removeCountryAndDelimiter(String name, String delimiter) {
		
		if (name.lastIndexOf(delimiter) > 0) {
			name = name.substring(name.lastIndexOf(delimiter) + 1).trim();
		}
		return name;
	}
	
}
