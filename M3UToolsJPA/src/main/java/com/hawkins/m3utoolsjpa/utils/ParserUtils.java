package com.hawkins.m3utoolsjpa.utils;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.springframework.util.StopWatch;

import com.hawkins.m3utoolsjpa.data.M3UGroup;
import com.hawkins.m3utoolsjpa.data.M3UItem;
import com.hawkins.m3utoolsjpa.parser.ParsingException;
import com.hawkins.m3utoolsjpa.properties.DownloadProperties;
import com.hawkins.m3utoolsjpa.regex.PatternMatcher;
import com.hawkins.m3utoolsjpa.regex.Patterns;
import com.hawkins.m3utoolsjpa.regex.RegexUtils;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ParserUtils {

	/**
	 * Extract unique tvg-groups from the parsed M3U file
	 *
	 * @return Set of unique tvg-groups
	 */

	public static Set<M3UGroup> extractUniqueTvgGroups(LinkedList<M3UItem> m3uItems) {
		Set<M3UGroup> uniqueTvgGroups = new HashSet<>();

		for (M3UItem item : m3uItems) {
			String groupTitle = item.getGroupTitle();
			if (groupTitle != null && !groupTitle.isEmpty()) {
				M3UGroup group = new M3UGroup(groupTitle, Utils.deriveGroupTypeByUrl(item.getChannelUri()));
				if (!uniqueTvgGroups.contains(group)) {
					uniqueTvgGroups.add(group);
				}
			}
		}

		log.info("Number of unique tvg-groups: {}", uniqueTvgGroups.size());
		return uniqueTvgGroups;
	}

	public static void loadM3UFileFromUrl(String fileUrl, String outputFileName) {
		HttpURLConnection connection = null;
		BufferedInputStream in = null;
		FileOutputStream out = null;

		try {
			URL url = new URI(fileUrl).toURL();
			connection = (HttpURLConnection) url.openConnection();
			connection.setRequestMethod("GET");

			in = new BufferedInputStream(connection.getInputStream());
			out = new FileOutputStream(outputFileName);

			byte[] buffer = new byte[1024];
			int bytesRead;
			while ((bytesRead = in.read(buffer, 0, 1024)) != -1) {
				out.write(buffer, 0, bytesRead);
			}

			log.info("M3U file downloaded successfully to " + outputFileName);
		} catch (IOException | URISyntaxException e) {
			log.error("Error downloading M3U file: " + e.getMessage());
		} finally {
			try {
				if (in != null) in.close();
				if (out != null) out.close();
				if (connection != null) connection.disconnect();
			} catch (IOException e) {
				log.error("Error closing resources: " + e.getMessage());
			}
		}

	}
	
	public static LinkedList<M3UItem> parse() {

		int lineNbr = 0;
		String line;
		LinkedList<M3UItem> entries = new LinkedList<M3UItem>();
		DownloadProperties dp = DownloadProperties.getInstance();
		String[] includedCountries = dp.getIncludedCountries();
		
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
			try {
				FileDownloader.downloadFileInSegments(dp.getStreamChannels(), m3uFileOnDisk.toString(), dp.getBufferSize());
			} catch (IOException | InterruptedException e) {
				log.info("Error in parse: " + e.getMessage());
			}
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
					entry = extractExtInfo(patternMatcher, line, includedCountries);
				} else {
					if (entry != null) {
						String groupTitle = entry.getGroupTitle();
						if (isIncludedCountry(includedCountries, groupTitle)) {
							String type = Utils.deriveGroupTypeByUrl(line);
							entry.setType(type);
							entry.setChannelUri(line);
							entry.setChannelName(RegexUtils.removeCountryIdentifier(entry.getChannelName(),
									dp.getIncludedCountries()));
							entry.setTvgName(
									RegexUtils.removeCountryIdentifier(entry.getTvgName(), dp.getIncludedCountries()));
							entries.add(entry);
						}
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
        if (tvgName == null || tvgName.startsWith("#####") || tvgName.isEmpty()) return null;

        String groupTitle = patternMatcher.extract(line, Patterns.GROUP_TITLE_REGEX);
        if (groupTitle == null || !ParserUtils.isIncludedCountry(includedCountries, groupTitle)) return null;

        tvgName = StringUtils.cleanTextContent(StringUtils.removeCountryIdentifierUsingRegExpr(tvgName, dp.getCountryRegExpr()));
        String channelName = StringUtils.cleanTextContent(StringUtils.removeCountryIdentifierUsingRegExpr(patternMatcher.extract(line, Patterns.CHANNEL_NAME_REGEX), dp.getCountryRegExpr()));

        return new M3UItem(
            patternMatcher.extract(line, Patterns.DURATION_REGEX),
            groupTitle,
            -1L,
            patternMatcher.extract(line, Patterns.TVG_ID_REGEX),
            tvgName,
            "",
            patternMatcher.extract(line, Patterns.TVG_LOGO_REGEX),
            patternMatcher.extract(line, Patterns.TVG_SHIFT_REGEX),
            patternMatcher.extract(line, Patterns.RADIO_REGEX),
            "",
            channelName,
            "",
            channelName,
            false
        );
    }
	
	public static LinkedList	<M3UItem> createM3UItemsListIfGroupExists(Set<M3UGroup> uniqueGroups, List<M3UItem> m3uItems) {
        LinkedList<M3UItem> filteredItems = new LinkedList<>();
        for (M3UItem item : m3uItems) {
            for (M3UGroup group : uniqueGroups) {
                if (group.getName().equals(item.getGroupTitle())) {
                	item.setGroupId(group.getId());
                    filteredItems.add(item);
                    break;
                }
            }
        }
        return filteredItems;
    }
	
	public static boolean isIncludedCountry(String[] includedCountries, String country) {
		for (String includedCountry : includedCountries) {
			if (country.startsWith(includedCountry)) {
				return true;
			}
		}
		return false;
	}
}


