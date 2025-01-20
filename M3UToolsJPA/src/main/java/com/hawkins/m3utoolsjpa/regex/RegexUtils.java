
package com.hawkins.m3utoolsjpa.regex;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RegexUtils {

    public static boolean containsRegex(String stringToTest, Pattern pattern) {
        Matcher matcher = pattern.matcher(stringToTest);
        return matcher.matches();
    }

    public static String removeCountryIdentifier(String stringToParse, String[] charactersToMatch) {
        String regexPattern = String.join("|", charactersToMatch);
        Pattern pattern = Pattern.compile("^(" + regexPattern + ")");
        Matcher matcher = pattern.matcher(stringToParse);

        if (matcher.find()) {
            stringToParse = stringToParse.substring(matcher.end()).trim();
        }

        return stringToParse;
    }

    public static boolean containsCountryIdentifier(String stringToParse, String[] charactersToMatch) {
        String regexPattern = String.join("|", charactersToMatch);
        Pattern pattern = Pattern.compile("^(" + regexPattern + ")");
        return pattern.matcher(stringToParse).find();
    }
}
