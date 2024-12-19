package com.hawkins.m3utoolsjpa.utils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.hawkins.m3utoolsjpa.regex.Patterns;

public class StringUtils {

    private StringUtils() {
        throw new IllegalStateException("Utility class");
    }

    // Check if the string is null or empty
    public static boolean isNullOrEmpty(String str) {
        return str == null || str.isEmpty();
    }

    // Check if the string is null, empty, or blank (ignoring whitespace)
    public static boolean isNullOrEmptyOrBlank(String str) {
        return str == null || str.trim().isEmpty();
    }

    // Convert StringBuffer to byte array
    public static byte[] getBytes(StringBuffer sb) {
        return sb == null ? new byte[0] : sb.toString().getBytes();
    }

    // Convert StringBuilder to byte array
    public static byte[] getBytes(StringBuilder sb) {
        return sb == null ? new byte[0] : sb.toString().getBytes();
    }

    // Convert String to byte array
    public static byte[] getBytes(String s) {
        return s == null ? new byte[0] : s.getBytes();
    }

    // Remove country identifier from the string using predefined pattern
    public static String removeCountryIdentifier(String toParse) {
        if (toParse == null) return null;
        Matcher matcher = Patterns.STRIP_COUNTRY_IDENTIFIER.matcher(toParse);
        return matcher.find() ? matcher.replaceFirst("") : toParse;
    }

    // Remove country identifier using custom regular expression
    public static String removeCountryIdentifierUsingRegExpr(String toParse, String regExpr) {
        if (toParse == null || regExpr == null) return toParse;
        Matcher matcher = Pattern.compile(regExpr).matcher(toParse);
        return matcher.find() ? matcher.replaceFirst("") : toParse;
    }

    // Clean text content by stripping non-ASCII, control characters, and non-printable Unicode characters
    public static String cleanTextContent(String text) {
        if (text == null) return null;
        // Combining regular expressions to perform all cleaning operations in a single replaceAll
        return text.replaceAll("[^\\x00-\\x7F\\r\\n\\t]|\\p{Cntrl}|\\p{C}", "").trim();
    }

    // Remove the country and delimiter (based on the last occurrence of the delimiter)
    public static String removeCountryAndDelimiter(String name, String delimiter) {
        if (name == null || delimiter == null || !name.contains(delimiter)) {
            return name; // Return the original name if delimiter is not found
        }
        return name.substring(name.lastIndexOf(delimiter) + 1).trim();
    }
}
