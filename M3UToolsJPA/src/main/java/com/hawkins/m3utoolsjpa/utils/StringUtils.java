
package com.hawkins.m3utoolsjpa.utils;

import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.hawkins.m3utoolsjpa.regex.Patterns;

public class StringUtils {

    private StringUtils() {
        throw new IllegalStateException("Utility class");
    }

    public static boolean isNullOrEmpty(String str) {
        return str == null || str.isEmpty();
    }

    public static boolean isNullOrEmptyOrBlank(String str) {
        return str == null || str.trim().isEmpty();
    }

    public static byte[] getBytes(StringBuffer sb) {
        Objects.requireNonNull(sb, "StringBuffer cannot be null");
        return sb.toString().getBytes(StandardCharsets.UTF_8);
    }

    public static byte[] getBytes(StringBuilder sb) {
        Objects.requireNonNull(sb, "StringBuilder cannot be null");
        return sb.toString().getBytes(StandardCharsets.UTF_8);
    }

    public static byte[] getBytes(String s) {
        Objects.requireNonNull(s, "String cannot be null");
        return s.getBytes(StandardCharsets.UTF_8);
    }

    public static String removeCountryIdentifier(String toParse) {
        Objects.requireNonNull(toParse, "String to parse cannot be null");
        Matcher matcher = Patterns.STRIP_COUNTRY_IDENTIFIER.matcher(toParse);
        if (matcher.find()) {
            toParse = matcher.replaceFirst("");
        }
        return toParse;
    }

    public static String removeCountryIdentifierUsingRegExpr(String toParse, String regExpr) {
        Objects.requireNonNull(toParse, "String to parse cannot be null");
        Objects.requireNonNull(regExpr, "Regular expression cannot be null");
        Matcher matcher = Pattern.compile(regExpr).matcher(toParse);
        if (matcher.find()) {
            toParse = matcher.replaceFirst("");
        }
        return toParse;
    }

    public static String cleanTextContent(String text) {
        Objects.requireNonNull(text, "Text cannot be null");
        return text.replaceAll("[^\\x00-\\x7F]|[\\p{Cntrl}&&[^\r\n\t]]|\\p{C}", "").trim();
    }

    public static String removeCountryAndDelimiter(String name, String delimiter) {
        Objects.requireNonNull(name, "Name cannot be null");
        Objects.requireNonNull(delimiter, "Delimiter cannot be null");
        int lastIndex = name.lastIndexOf(delimiter);
        if (lastIndex > 0) {
            name = name.substring(lastIndex + 1).trim();
        }
        return name;
    }
}
