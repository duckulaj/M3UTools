
package com.hawkins.m3utoolsjpa.regex;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RegexUtils {

    private static final Pattern COUNTRY_IDENTIFIER_PATTERN = Pattern.compile("^(%s)");

    public static boolean containsRegex(String stringToTest, Pattern pattern) {
        Matcher matcher = pattern.matcher(stringToTest);
        return matcher.matches();
    }

    public static String removeCountryIdentifier(String stringToParse, String[] charactersToMatch) {
        String regexPattern = String.join("|", charactersToMatch);
        Pattern pattern = Pattern.compile(String.format(COUNTRY_IDENTIFIER_PATTERN.pattern(), regexPattern));
        Matcher matcher = pattern.matcher(stringToParse);

        if (matcher.find()) {
            stringToParse = stringToParse.substring(matcher.end()).trim();
        }

        return stringToParse;
    }

    public static boolean containsCountryIdentifier(String stringToParse, String[] charactersToMatch) {
        String regexPattern = String.join("|", charactersToMatch);
        Pattern pattern = Pattern.compile(String.format(COUNTRY_IDENTIFIER_PATTERN.pattern(), regexPattern));
        return pattern.matcher(stringToParse).find();
    }
}
