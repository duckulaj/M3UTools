package com.hawkins.m3utoolsjpa.parser;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.util.LinkedList;

import org.springframework.util.StopWatch;

import com.hawkins.m3utoolsjpa.data.M3UItem;
import com.hawkins.m3utoolsjpa.properties.DownloadProperties;
import com.hawkins.m3utoolsjpa.regex.PatternMatcher;
import com.hawkins.m3utoolsjpa.regex.Patterns;
import com.hawkins.m3utoolsjpa.utils.Constants;
import com.hawkins.m3utoolsjpa.utils.FileUtilsForM3UToolsJPA;
import com.hawkins.m3utoolsjpa.utils.StringUtils;
import com.hawkins.m3utoolsjpa.utils.Utils;

import lombok.extern.slf4j.Slf4j;

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

		boolean getRemoteM3U = false;

		File m3uFileOnDisk = new File(Constants.M3U_FILE);

		try {

			getRemoteM3U = Utils.fileOlderThan(m3uFileOnDisk, dp.getFileAgeM3U());

		} catch (MalformedURLException e) {
			throw new ParsingException(lineNbr, "Cannot open URL", e);
		} catch (IOException e) {
			log.info("File {} not found", m3uFileOnDisk.toString());
			getRemoteM3U = true;
		}

		if (getRemoteM3U) {
			
			if (m3uFileOnDisk.exists()) FileUtilsForM3UToolsJPA.backupFile(m3uFileOnDisk.toString());
			
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

			String[] includedCountries = dp.getIncludedCountries();

			while ((line = buffer.readLine()) != null) {
				lineNbr++;
				if (isExtInfo(line)) {
					entry = extractExtInfo(patternMatcher, line, includedCountries);
				} else {
					if (entry != null) {
						String type = Utils.deriveGroupTypeByUrl(line);
						entry.setType(type);
						entry.setChannelUri(line);
						entries.add(entry);
					}
				}
			}
		} catch (IOException e) {
			FileUtilsForM3UToolsJPA.restoreFile(m3uFileOnDisk.toString());
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

	private static M3UItem extractExtInfo(PatternMatcher patternMatcher, String line, String[] includedCountries) {

		DownloadProperties dp = DownloadProperties.getInstance();
		
		String tvgName = patternMatcher.extract(line, Patterns.TVG_NAME_REGEX);
		// String tvgName = patternMatcher.extract(line, Patterns.SQUARE_BRACKET_COUNTRY);

		if (tvgName.startsWith("#####")) return null;

		int endIndex = -1;
		
		if (tvgName.length() ==  0) {
			return null;
		}
		
		endIndex = Utils.indexOfAny(tvgName.substring(0, 3), includedCountries);

		if (endIndex == -1) return null;

		tvgName = StringUtils.removeCountryIdentifierUsingRegExpr(tvgName, dp.getCountryRegExpr());
		tvgName = StringUtils.cleanTextContent(tvgName);

		String channelName = patternMatcher.extract(line, Patterns.CHANNEL_NAME_REGEX);
		channelName = StringUtils.removeCountryIdentifierUsingRegExpr(channelName, dp.getCountryRegExpr());
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


}
