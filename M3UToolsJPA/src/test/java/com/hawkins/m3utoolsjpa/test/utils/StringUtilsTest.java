package com.hawkins.m3utoolsjpa.test.utils;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import com.hawkins.m3utoolsjpa.utils.StringUtils;

class StringUtilsTest {

	@Test
	void testRemoveCountryIdentifier() {
		
		String expected = "123";
		String actual = StringUtils.removeCountryIdentifier("EN - 123");
		
		assertEquals(expected, actual);		
		
	}

	@Test
	void removeCountryAndDelimiter() {
		
		String expected = "Mission Impossible";
		String actual = StringUtils.removeCountryAndDelimiter("EN| Mission Impossible", "|");
		
		assertEquals(expected, actual);
	}
	
	
}
