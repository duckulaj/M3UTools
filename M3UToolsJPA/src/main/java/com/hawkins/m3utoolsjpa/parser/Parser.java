package com.hawkins.m3utoolsjpa.parser;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.util.LinkedList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.util.StopWatch;

import com.hawkins.m3utoolsjpa.data.M3UItem;
import com.hawkins.m3utoolsjpa.properties.DownloadProperties;
import com.hawkins.m3utoolsjpa.regex.PatternMatcher;
import com.hawkins.m3utoolsjpa.regex.Patterns;
import com.hawkins.m3utoolsjpa.utils.Constants;
import com.hawkins.m3utoolsjpa.utils.StringUtils;
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
	
	private static DownloadProperties dp = DownloadProperties.getInstance();
	
	public static LinkedList<M3UItem> parse() {

		int lineNbr = 0;
		String line;
		LinkedList<M3UItem> entries = new LinkedList<M3UItem>();
		DownloadProperties dp = DownloadProperties.getInstance();

		StopWatch sw = new org.springframework.util.StopWatch();
		sw.start();

		boolean getRemoteM3U = false;
		URL m3uUrl = null;
		File m3uFileOnDisk = new File(Constants.M3U_FILE);

		try {
			m3uUrl = new  URI(dp.getStreamChannels()).toURL();

			try {
				getRemoteM3U = Utils.fileOlderThan(m3uFileOnDisk, dp.getFileAgeM3U());
			} catch (IOException ex) {
				log.info("File {} does not exist", m3uFileOnDisk);
				getRemoteM3U = true;
			}
		} catch (MalformedURLException e) {
			throw new ParsingException(lineNbr, "Cannot open URL", e);
		} catch (URISyntaxException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		if (getRemoteM3U) {
			log.info("Retrieving {} from remote server", m3uFileOnDisk.toString());
			Utils.copyUrlToFileUsingCommonsIO(dp.getStreamChannels(), m3uFileOnDisk.toString());
		}

		try (var buffer = Files.newBufferedReader(m3uFileOnDisk.toPath())) {

			line = buffer.readLine();
			if (line == null) {
				throw new ParsingException(0, "Empty stream");
			}

			if (line.trim().equalsIgnoreCase("")) {
				line = buffer.readLine();
			}
			lineNbr++;

			checkStart(line);

			PatternMatcher patternMatcher = PatternMatcher.getInstance();
			M3UItem entry = null;
			while ((line = buffer.readLine()) != null) {
				lineNbr++;
				if (isExtInfo(line)) {
					entry = extractExtInfo(patternMatcher, line);
					// entry = extractExtInfoRaw(patternMatcher, line);
				} else {
					if (entry != null) {
						String type = Utils.deriveGroupTypeByUrl(line);
						entry.setType(type);
						entry.setChannelUri(line);

						/*
						if (entry.getType().equals(Constants.MOVIE)) {
							entry.setSearch(Utils.normaliseSearch(entry.getChannelName()));
						}
						*/

						// if (!RegexUtils.containsRegex(entry.getTvgName(), Patterns.HASH_REGEX) || (!RegexUtils.containsRegex(entry.getChannelName(), Patterns.ADULT_REGEX)) ) {
						
						entries.add(entry);
						// }

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

	private static M3UItem extractExtInfo(PatternMatcher patternMatcher, String line) {

		String tvgName = patternMatcher.extract(line, Patterns.TVG_NAME_REGEX);

		if (tvgName.startsWith("#####")) return null;
		
		int endIndex = Utils.indexOfAny(tvgName.substring(0, 2), dp.getIncludedCountries());
		
		if (endIndex == -1) return null;
		
		tvgName = StringUtils.removeCountryAndDelimiter(tvgName, "|");
		tvgName = StringUtils.cleanTextContent(tvgName);
		
		String channelName = patternMatcher.extract(line, Patterns.CHANNEL_NAME_REGEX);
		channelName = StringUtils.removeCountryAndDelimiter(channelName, "|");
		channelName = StringUtils.cleanTextContent(channelName);
		
		String duration = patternMatcher.extract(line, Patterns.DURATION_REGEX);
		String tvgId = patternMatcher.extract(line, Patterns.TVG_ID_REGEX);
		String tvgShift = patternMatcher.extract(line, Patterns.TVG_SHIFT_REGEX);
		String radio = patternMatcher.extract(line, Patterns.RADIO_REGEX);
		String tvgLogo = patternMatcher.extract(line, Patterns.TVG_LOGO_REGEX);
		String groupTitle = patternMatcher.extract(line, Patterns.GROUP_TITLE_REGEX);
		Long groupId = -1L;
		
		

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


	private static M3UItem extractExtInfoRaw(PatternMatcher patternMatcher, String line) {

		String tvgName = extract(line, Patterns.TVG_NAME_REGEX);

		
		if (tvgName.startsWith("#####")) return null;
		
		String duration = patternMatcher.extract(line, Patterns.DURATION_REGEX);
		String groupTitle = patternMatcher.extract(line, Patterns.GROUP_TITLE_REGEX);
		Long groupId = -1L;
		String tvgId = patternMatcher.extract(line, Patterns.TVG_ID_REGEX);
		String tvgChNo = patternMatcher.extract(line, Patterns.TVG_CHANNEL_NUMBER);
		String tvgLogo = patternMatcher.extract(line, Patterns.TVG_LOGO_REGEX);
		String tvgShift = patternMatcher.extract(line, Patterns.TVG_SHIFT_REGEX);
		String radio = patternMatcher.extract(line, Patterns.RADIO_REGEX);
		String channelUri = "";
		String channelName = patternMatcher.extract(line, Patterns.CHANNEL_NAME_REGEX);
		String type = "";
		String search = channelName;
		boolean selected = false;
		
		
		
		
		
		

		return new M3UItem(
				duration,
				groupTitle,
				groupId,
				tvgId,
				tvgName,
				tvgChNo,
				tvgLogo,
				tvgShift,
				radio,
				channelUri,
				channelName,
				type,
				search,
				selected);

	}





}
