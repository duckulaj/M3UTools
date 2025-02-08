package com.hawkins.m3utoolsjpa.test.utils;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.hawkins.m3utoolsjpa.utils.StringUtils;
import java.nio.charset.StandardCharsets;

import org.junit.jupiter.api.Test;



public class StringUtilsTest {

    @Test
    public void testIsNullOrEmpty() {
        assertTrue(StringUtils.isNullOrEmpty(null));
        assertTrue(StringUtils.isNullOrEmpty(""));
        assertFalse(StringUtils.isNullOrEmpty("test"));
    }

    @Test
    public void testIsNullOrEmptyOrBlank() {
        assertTrue(StringUtils.isNullOrEmptyOrBlank(null));
        assertTrue(StringUtils.isNullOrEmptyOrBlank(""));
        assertTrue(StringUtils.isNullOrEmptyOrBlank("   "));
        assertFalse(StringUtils.isNullOrEmptyOrBlank("test"));
    }

    @Test
    public void testGetBytesFromStringBuffer() {
        StringBuffer sb = new StringBuffer("test");
        byte[] bytes = StringUtils.getBytes(sb);
        assertArrayEquals("test".getBytes(StandardCharsets.UTF_8), bytes);
    }

    @Test
    public void testGetBytesFromStringBuilder() {
        StringBuilder sb = new StringBuilder("test");
        byte[] bytes = StringUtils.getBytes(sb);
        assertArrayEquals("test".getBytes(StandardCharsets.UTF_8), bytes);
    }

    @Test
    public void testGetBytesFromString() {
        String s = "test";
        byte[] bytes = StringUtils.getBytes(s);
        assertArrayEquals("test".getBytes(StandardCharsets.UTF_8), bytes);
    }

    @Test
    public void testRemoveCountryIdentifier() {
        String result = StringUtils.removeCountryIdentifier("US - test");
        assertEquals("test", result);
    }

    @Test
    public void testRemoveCountryIdentifierUsingRegExpr() {
        String result = StringUtils.removeCountryIdentifierUsingRegExpr("US_test", "US_");
        assertEquals("test", result);
    }

    @Test
    public void testCleanTextContent() {
        String result = StringUtils.cleanTextContent("test\u0000\u0001\u0002");
        assertEquals("test", result);
    }

    @Test
    public void testRemoveCountryAndDelimiter() {
        String result = StringUtils.removeCountryAndDelimiter("US_test", "_");
        assertEquals("test", result);
    }
}
