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
	public static final Pattern GROUP_TITLE_REGEX = Pattern.compile(".*group-title=\"(.?|.+?)\".*", Pattern.CASE_INSENSITIVE);
	public static final Pattern RADIO_REGEX = Pattern.compile(".*radio=\"(.?|.+?)\".*", Pattern.CASE_INSENSITIVE);
	public static final Pattern CHANNEL_NAME_REGEX = Pattern.compile(".*,(.+?)$", Pattern.CASE_INSENSITIVE);
	public static final Pattern SQUARE_BRACKET_REGEX = Pattern.compile("\\[.*?\\]", Pattern.CASE_INSENSITIVE);
	public static final Pattern PIPES_REGEX = Pattern.compile("([^|]*\\|){1}([^|]*\\|)", Pattern.CASE_INSENSITIVE);
	public static final Pattern PIPE_REGEX = Pattern.compile("([^|]*\\|{1}[^|]{1}[^|])", Pattern.CASE_INSENSITIVE);

}
