
package com.hawkins.m3utoolsjpa.regex;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RegexUtils {

    private static final Pattern COUNTRY_IDENTIFIER_PATTERN = Pattern.compile("^(%s)");

    public static boolean matchesPattern(String input, Pattern pattern) {
        return pattern.matcher(input).matches();
    }

    public static String removeCountryIdentifier(String input, String[] identifiers) {
        String regexPattern = String.join("|", identifiers);
        Pattern pattern = Pattern.compile(String.format(COUNTRY_IDENTIFIER_PATTERN.pattern(), regexPattern));
        Matcher matcher = pattern.matcher(input);

        if (matcher.find()) {
            input = input.substring(matcher.end()).trim();
        }

        return input;
    }

    public static boolean containsCountryIdentifier(String input, String[] identifiers) {
        String regexPattern = String.join("|", identifiers);
        Pattern pattern = Pattern.compile(String.format(COUNTRY_IDENTIFIER_PATTERN.pattern(), regexPattern));
        return pattern.matcher(input).find();
    }
}
