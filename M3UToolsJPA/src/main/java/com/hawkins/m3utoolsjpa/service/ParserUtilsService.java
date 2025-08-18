package com.hawkins.m3utoolsjpa.service;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.springframework.stereotype.Service;
import org.springframework.util.StopWatch;

import com.hawkins.m3utoolsjpa.annotations.TrackExecutionTime;
import com.hawkins.m3utoolsjpa.data.M3UGroup;
import com.hawkins.m3utoolsjpa.data.M3UItem;
import com.hawkins.m3utoolsjpa.exception.DownloadFailureException;
import com.hawkins.m3utoolsjpa.parser.ParsingException;
import com.hawkins.m3utoolsjpa.properties.DownloadProperties;
import com.hawkins.m3utoolsjpa.regex.PatternMatcher;
import com.hawkins.m3utoolsjpa.regex.Patterns;
import com.hawkins.m3utoolsjpa.regex.RegexUtils;
import com.hawkins.m3utoolsjpa.utils.Constants;
import com.hawkins.m3utoolsjpa.utils.FileUtilsForM3UToolsJPA;
import com.hawkins.m3utoolsjpa.utils.StringUtils;
import com.hawkins.m3utoolsjpa.utils.Utils;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service	
public class ParserUtilsService {

	/**
	 * Extract unique tvg-groups from the parsed M3U file
	 *
	 * @return Set of unique tvg-groups
	 */

	@TrackExecutionTime
	public Set<M3UGroup> extractUniqueTvgGroups(Set<M3UItem> m3uItems) {
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

		return uniqueTvgGroups;
	}

	@TrackExecutionTime 
	public void loadM3UFileFromUrl(String fileUrl, String outputFileName) {
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
	
	@TrackExecutionTime
	public Set<M3UItem> parse() throws DownloadFailureException {

		int lineNbr = 0;
		String line;
		Set<M3UItem> entries = new HashSet<>();
		DownloadProperties dp = DownloadProperties.getInstance();
		String[] includedCountries = dp.getIncludedCountries();
		File m3uFileOnDisk = new File(Constants.M3U_FILE);
		
		StopWatch sw = new org.springframework.util.StopWatch();
		sw.start();

		FileUtilsForM3UToolsJPA.getM3UFile(m3uFileOnDisk);

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
							String commonName = RegexUtils.removeCountryIdentifier(entry.getTvgName(), dp.getIncludedCountries());
							
							entry.setChannelName(commonName);
							entry.setTvgName(commonName);
							entry.setSearch(commonName);
							entries.add(entry);
							entry = null;
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
	private void checkStart(String line) {
		if (line != null) {
			if (!line.contains(Patterns.M3U_START_MARKER)) {
				throw new ParsingException(1, "First line of the file should be " + Patterns.M3U_START_MARKER);
			}
		}
	}

	private boolean isExtInfo(String line) {
		return line.contains(Patterns.M3U_INFO_MARKER);
	}

	private M3UItem extractExtInfo(PatternMatcher patternMatcher, String line, String[] includedCountries) {
        DownloadProperties dp = DownloadProperties.getInstance();

        String tvgName = patternMatcher.extract(line, Patterns.TVG_NAME_REGEX);
        if (tvgName == null || tvgName.startsWith("#####") || tvgName.isEmpty()) return null;

        String groupTitle = patternMatcher.extract(line, Patterns.GROUP_TITLE_REGEX);
        if (groupTitle == null || !isIncludedCountry(includedCountries, groupTitle)) return null;

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
	
	@TrackExecutionTime
	public Set<M3UItem> createM3UItemsListIfGroupExists(Set<M3UGroup> uniqueGroups, Set<M3UItem> m3uItems) {
	    Set<M3UItem> filteredItems = new HashSet<>();
	    Map<String, M3UGroup> groupMap = new HashMap<>();

	    for (M3UGroup group : uniqueGroups) {
	        groupMap.put(group.getName(), group);
	    }

	    for (M3UItem item : m3uItems) {
	        M3UGroup group = groupMap.get(item.getGroupTitle());
	        if (group != null) {
	            item.setGroupId(group.getId());
	            filteredItems.add(item);
	        }
	    }

	    return filteredItems;
	}
	
	public boolean isIncludedCountry(String[] includedCountries, String country) {
		for (String includedCountry : includedCountries) {
			if (country.startsWith(includedCountry)) {
				return true;
			}
		}
		return false;
	}

}
