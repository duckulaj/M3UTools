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
import com.hawkins.m3utoolsjpa.regex.Patterns;
import com.hawkins.m3utoolsjpa.regex.RegexUtils;
import com.hawkins.m3utoolsjpa.utils.Constants;
import com.hawkins.m3utoolsjpa.utils.Utils;

import lombok.extern.slf4j.Slf4j;

/**
 * Created by tgermain on 30/12/2017.
 */
@Slf4j
public class Parser {

	/**
	 * Parse the m3u file
	 *
	 * @param stream pointing to your m3u file
	 * @return Linked List of m3uItems found within the supplied m3uFile
	 */
	public static LinkedList<M3UItem> parse() {

		StopWatch sw = new org.springframework.util.StopWatch();
		sw.start();
		
		Utils.copyUrlToFile(DownloadProperties.getInstance().getStreamChannels(), "./channels.m3u");
		// String m3uFile = DownloadProperties.getInstance().getFullM3U();
		String m3uFile = "./channels.m3u";
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
			
			if (line.trim().equalsIgnoreCase("")) {
				line = buffer.readLine();
			}
			lineNbr++;
			
			checkStart(line);
			
			String globalTvgShif = extract(line, Patterns.TVG_SHIFT_REGEX);

			M3UItem entry = null;
			while ((line = buffer.readLine()) != null) {
				lineNbr++;
				if (isExtInfo(line)) {
					entry = extractExtInfo(globalTvgShif, line);
				} else {
					if (entry == null) {
						throw new ParsingException(lineNbr, "Missing " + Patterns.M3U_INFO_MARKER);
					}
					String type = deriveGroupTypeByUrl(line);
					entry.setType(type);
					entry.setChannelUri(line);
					
					if (entry.getType().equals(Constants.MOVIE)) {
						entry.setSearch(Utils.normaliseName(entry.getChannelName()));
					}
					
					if (!RegexUtils.containsRegex(entry.getTvgName(), Patterns.HASH_REGEX) || (!RegexUtils.containsRegex(entry.getChannelName(), Patterns.ADULT_REGEX)) ) {
						entries.add(entry);
					}
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
			if (!line.contains(Patterns.M3U_START_MARKER)) {
				throw new ParsingException(1, "First line of the file should be " + Patterns.M3U_START_MARKER);
			}
		}
	}

	private static boolean isExtInfo(String line) {
		return line.contains(Patterns.M3U_INFO_MARKER);
	}

	private static M3UItem extractExtInfo(String globalTvgShift, String line) {
		String duration = extract(line, Patterns.DURATION_REGEX);
		String tvgId = extract(line, Patterns.TVG_ID_REGEX);
		String tvgName = Utils.removeFromString((extract(line, Patterns.TVG_NAME_REGEX)), Patterns.VALID_CHANNEL_NAME);
		
		String tvgShift = extract(line, Patterns.TVG_SHIFT_REGEX);
		if (tvgShift == null) {
			tvgShift = globalTvgShift;
		}
		String radio = extract(line, Patterns.RADIO_REGEX);
		String tvgLogo = extract(line, Patterns.TVG_LOGO_REGEX);
		String groupTitle = extract(line, Patterns.GROUP_TITLE_REGEX);
		Long groupId = -1L;
		String channelName = Utils.removeFromString((extract(line, Patterns.CHANNEL_NAME_REGEX)), Patterns.VALID_CHANNEL_NAME);

		
		// Remove |EN|
		return new M3UItem(
				duration,
				groupTitle,
				groupId,
				tvgId,
				tvgName,
				"",
				tvgLogo,
				tvgShift,
				radio,
				"",
				channelName,
				"",
				"",
				false);
				
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
