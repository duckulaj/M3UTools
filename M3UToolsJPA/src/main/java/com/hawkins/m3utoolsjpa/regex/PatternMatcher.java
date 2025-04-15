
package com.hawkins.m3utoolsjpa.regex;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PatternMatcher implements Runnable {

    private static class SingletonHelper {
        private static final PatternMatcher INSTANCE = new PatternMatcher();
    }

    private static final ThreadLocal<Matcher> threadLocalMatcher = ThreadLocal.withInitial(() -> Pattern.compile("").matcher(""));

    private PatternMatcher() {
        // Private constructor to prevent instantiation
    }

    public static PatternMatcher getInstance() {
        return SingletonHelper.INSTANCE;
    }

    public String extract(String line, Pattern pattern) {
        Matcher matcher = threadLocalMatcher.get();
        matcher.reset(line);
        matcher.usePattern(pattern);
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
