package com.hawkins.m3utoolsjpa.regex;

import java.util.regex.Pattern;

public class Patterns {

	public static final String M3U_START_MARKER = "#EXTM3U";
	public static final String M3U_INFO_MARKER = "#EXTINF:";
	public static final Pattern DURATION_REGEX = Pattern.compile(".*#EXTINF:(.+?) .*", Pattern.CASE_INSENSITIVE);
	public static final Pattern TVG_ID_REGEX = Pattern.compile(".*tvg-id=\"(.?|.+?)\".*", Pattern.CASE_INSENSITIVE);
	public static final Pattern TVG_NAME_REGEX = Pattern.compile(".*tvg-name=\"(.?|.+?)\".*", Pattern.CASE_INSENSITIVE);
	public static final Pattern TVG_LOGO_REGEX = Pattern.compile(".*tvg-logo=\"(.?|.+?)\".*", Pattern.CASE_INSENSITIVE);
	public static final Pattern TVG_SHIFT_REGEX = Pattern.compile(".*tvg-shift=\"(.?|.+?)\".*", Pattern.CASE_INSENSITIVE);
	public static final Pattern TVG_CHANNEL_NUMBER = Pattern.compile(".*tvg-chno=\"(.?|.+?)\".*", Pattern.CASE_INSENSITIVE);
	public static final Pattern GROUP_TITLE_REGEX = Pattern.compile(".*group-title=\"(.?|.+?)\".*", Pattern.CASE_INSENSITIVE);
	public static final Pattern RADIO_REGEX = Pattern.compile(".*radio=\"(.?|.+?)\".*", Pattern.CASE_INSENSITIVE);
	public static final Pattern CHANNEL_NAME_REGEX = Pattern.compile(".*,(.+?)$", Pattern.CASE_INSENSITIVE);
	public static final Pattern SQUARE_BRACKET_REGEX = Pattern.compile("\\[.*?\\]", Pattern.CASE_INSENSITIVE);
	public static final Pattern PIPES_REGEX = Pattern.compile("([^|]*\\|){1}([^|]*\\|)", Pattern.CASE_INSENSITIVE);
	public static final Pattern PIPE_REGEX = Pattern.compile("([^|]*\\|{1}[^|]{1}[^|])", Pattern.CASE_INSENSITIVE);
	public static final Pattern ADULT_REGEX = Pattern.compile("\b(?:ADULT|For Adults)\b", Pattern.CASE_INSENSITIVE);
	public static final Pattern HASH_REGEX = Pattern.compile("[#]{5}");
	public static final Pattern VALID_CHANNEL_NAME = Pattern.compile("(?:\\w[A-Z]\\s[-]\\s)");
	//public static final Pattern VALID_CHANNEL_NAME = Pattern.compile("[^a-zA-Z0-9 -]");
	public static final Pattern STRIP_COUNTRY_IDENTIFIER = Pattern.compile(".{2,} - *");
	public static final Pattern FISHEYE_CHARACTER = Pattern.compile("[^\\x1F-\\x7F]+");
	public static final Pattern PIPED_COUNTRY = Pattern.compile("\\|.{2,}\\| ");
	public static final Pattern HYPHEN_REGEX = Pattern.compile("\\-.*?", Pattern.CASE_INSENSITIVE);
	public static final Pattern SKYCINEMA_REGEX = Pattern.compile("\\SC.*?");
}
