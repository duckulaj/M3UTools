package com.hawkins.m3utoolsjpa.parser;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
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

		int lineNbr = 0;
		String line;
		LinkedList<M3UItem> entries = new LinkedList<M3UItem>();
		DownloadProperties dp = DownloadProperties.getInstance();
		
		StopWatch sw = new org.springframework.util.StopWatch();
		sw.start();
		
		try { URL m3uUrl = new  URL(dp.getStreamChannels());
	        BufferedReader buffer = new BufferedReader(new InputStreamReader(m3uUrl.openStream()));		
			
			line = buffer.readLine();
			if (line == null) {
				throw new ParsingException(0, "Empty stream");
			}
			
			if (line.trim().equalsIgnoreCase("")) {
				line = buffer.readLine();
			}
			lineNbr++;
			
			checkStart(line);

			M3UItem entry = null;
			while ((line = buffer.readLine()) != null) {
				lineNbr++;
				if (isExtInfo(line)) {
					entry = extractExtInfo(line);
				} else {
					if (entry == null) {
						throw new ParsingException(lineNbr, "Missing " + Patterns.M3U_INFO_MARKER);
					}
					String type = Utils.deriveGroupTypeByUrl(line);
					entry.setType(type);
					entry.setChannelUri(line);
					
					if (entry.getType().equals(Constants.MOVIE)) {
						entry.setSearch(Utils.normaliseSearch(entry.getChannelName()));
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

	private static M3UItem extractExtInfo(String line) {
		String duration = extract(line, Patterns.DURATION_REGEX);
		String tvgId = extract(line, Patterns.TVG_ID_REGEX);
		String tvgName = Utils.removeFromString((extract(line, Patterns.TVG_NAME_REGEX)), Patterns.VALID_CHANNEL_NAME);
		String tvgShift = extract(line, Patterns.TVG_SHIFT_REGEX);
		String radio = extract(line, Patterns.RADIO_REGEX);
		String tvgLogo = extract(line, Patterns.TVG_LOGO_REGEX);
		String groupTitle = extract(line, Patterns.GROUP_TITLE_REGEX);
		Long groupId = -1L;
		String channelName = Utils.removeFromString((extract(line, Patterns.CHANNEL_NAME_REGEX)), Patterns.VALID_CHANNEL_NAME);
		channelName = Utils.removeFromString(channelName, Patterns.FISHEYE_CHARACTER);
		tvgName = Utils.removeFromString(tvgName, Patterns.FISHEYE_CHARACTER);
		tvgName = Utils.removeFromString(tvgName, Patterns.PIPED_COUNTRY);
		channelName = Utils.removeFromString(channelName, Patterns.PIPED_COUNTRY);
		
		if (channelName.contains("|EN| ")) {
			channelName = channelName.substring(5);
		}
		
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
				channelName,
				false);
				
	}

	private static String extract(String line, Pattern pattern) {
		Matcher matcher = pattern.matcher(line);
		if (matcher.matches()) {
			return matcher.group(1);
		}
		return null;
	}
	
	

	

	
}
