package com.hawkins.m3utoolsjpa.parser;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.LinkedList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.util.StopWatch;

import com.hawkins.m3utoolsjpa.data.M3UItem;
import com.hawkins.m3utoolsjpa.properties.DownloadProperties;
import com.hawkins.m3utoolsjpa.utils.Constants;

import lombok.extern.slf4j.Slf4j;

/**
 * Created by tgermain on 30/12/2017.
 */
@Slf4j
public class Parser {

	private static final String M3U_START_MARKER = "#EXTM3U";
	private static final String M3U_INFO_MARKER = "#EXTINF:";
	private static final Pattern DURATION_REGEX = Pattern.compile(".*#EXTINF:(.+?) .*", Pattern.CASE_INSENSITIVE);
	private static final Pattern TVG_ID_REGEX = Pattern.compile(".*tvg-id=\"(.?|.+?)\".*", Pattern.CASE_INSENSITIVE);
	private static final Pattern TVG_NAME_REGEX = Pattern.compile(".*tvg-name=\"(.?|.+?)\".*", Pattern.CASE_INSENSITIVE);
	private static final Pattern TVG_LOGO_REGEX = Pattern.compile(".*tvg-logo=\"(.?|.+?)\".*", Pattern.CASE_INSENSITIVE);
	private static final Pattern TVG_SHIFT_REGEX = Pattern.compile(".*tvg-shift=\"(.?|.+?)\".*", Pattern.CASE_INSENSITIVE);
	private static final Pattern GROUP_TITLE_REGEX = Pattern.compile(".*group-title=\"(.?|.+?)\".*", Pattern.CASE_INSENSITIVE);
	private static final Pattern RADIO_REGEX = Pattern.compile(".*radio=\"(.?|.+?)\".*", Pattern.CASE_INSENSITIVE);
	private static final Pattern CHANNEL_NAME_REGEX = Pattern.compile(".*,(.+?)$", Pattern.CASE_INSENSITIVE);
	private static final Pattern SQUARE_BRACKET_REGEX = Pattern.compile(".*\\[*\\]", Pattern.CASE_INSENSITIVE);
	private static final Pattern PIPE_REGEX = Pattern.compile(".*\\|*\\|", Pattern.CASE_INSENSITIVE);
	
	
	/**
	 * Parse the m3u file
	 *
	 * @param stream pointing to your m3u file
	 * @return Linked List of m3uItems found within the supplied m3uFile
	 */
	public static LinkedList<M3UItem> parse() {

		StopWatch sw = new org.springframework.util.StopWatch();
		sw.start();
		
		String m3uFile = DownloadProperties.getInstance().getFullM3U();

		if (m3uFile == null) {
			throw new ParsingException(0, "Cannot read m3uFile");
		}
		
		LinkedList<M3UItem> entries = new LinkedList<M3UItem>();
		
		int lineNbr = 0;
		String line;
		try (BufferedReader buffer = Files.newBufferedReader(Paths.get(m3uFile), StandardCharsets.UTF_8)) {

			line = buffer.readLine();
			if (line == null) {
				throw new ParsingException(0, "Empty stream");
			}
			lineNbr++;
			
			checkStart(line);
			
			String globalTvgShif = extract(line, TVG_SHIFT_REGEX);

			M3UItem entry = null;
			while ((line = buffer.readLine()) != null) {
				lineNbr++;
				if (isExtInfo(line)) {
					entry = extractExtInfo(globalTvgShif, line);
				} else {
					if (entry == null) {
						throw new ParsingException(lineNbr, "Missing " + M3U_INFO_MARKER);
					}
					String type = deriveGroupTypeByUrl(line);
					entry.setType(type);
					entry.setChannelUri(line);
					entries.add(entry);
				}
			}
		} catch (IOException e) {
			throw new ParsingException(lineNbr, "Cannot read file", e);
		} 

		sw.stop();
		log.info("Total time in milliseconds for parsing : {}", sw.getTotalTimeMillis());
		return entries;
	}

	/*
	 * If the m3uFile exists determine that the start of the file as #EXTM3U
	 */
	private static void checkStart(String line) {
		if (line != null) {
			if (!line.contains(M3U_START_MARKER)) {
				throw new ParsingException(1, "First line of the file should be " + M3U_START_MARKER);
			}
		}
	}

	private static boolean isExtInfo(String line) {
		return line.contains(M3U_INFO_MARKER);
	}

	private static M3UItem extractExtInfo(String globalTvgShift, String line) {
		String duration = extract(line, DURATION_REGEX);
		String tvgId = extract(line, TVG_ID_REGEX);
		String tvgName = extract(line, TVG_NAME_REGEX);
		String tvgShift = extract(line, TVG_SHIFT_REGEX);
		if (tvgShift == null) {
			tvgShift = globalTvgShift;
		}
		String radio = extract(line, RADIO_REGEX);
		String tvgLogo = extract(line, TVG_LOGO_REGEX);
		String groupTitle = extract(line, GROUP_TITLE_REGEX);
		Long groupId = -1L;
		String channelName = extract(line, CHANNEL_NAME_REGEX);

		return new M3UItem(
				duration,
				groupTitle,
				groupId,
				tvgId,
				tvgName,
				tvgLogo,
				tvgShift,
				radio,
				"",
				channelName,
				"",
				"");
				
	}

	private static String extract(String line, Pattern pattern) {
		Matcher matcher = pattern.matcher(line);
		if (matcher.matches()) {
			return matcher.group(1);
		}
		return null;
	}
	
	

	public static String deriveGroupTypeByUrl(String url) {

		String groupType = "";

		if (url.contains(Constants.SERIES)) {
			groupType = Constants.SERIES;
		} else if (url.contains(Constants.MOVIE)) {
			groupType = Constants.MOVIE;
		} else if (url.length() > 0) {
			groupType = Constants.LIVE;
		}

		return groupType;
	}

	
}
