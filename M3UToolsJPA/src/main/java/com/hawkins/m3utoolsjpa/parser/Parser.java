package com.hawkins.m3utoolsjpa.parser;

import java.io.BufferedReader;
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
import com.hawkins.m3utoolsjpa.regex.RegexUtils;
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
	    LinkedList<M3UItem> entries = new LinkedList<>();
	    DownloadProperties dp = DownloadProperties.getInstance();

	    // Timer to track the duration of the parsing operation
	    StopWatch sw = new org.springframework.util.StopWatch();
	    sw.start();

	    boolean getRemoteM3U = false;

	    // File to hold the M3U data
	    File m3uFileOnDisk = new File(Constants.M3U_FILE);

	    try {
	        // Check if the file is older than the allowed age and needs to be retrieved remotely
	        getRemoteM3U = Utils.fileOlderThan(m3uFileOnDisk, dp.getFileAgeM3U());
	    } catch (MalformedURLException e) {
	        throw new ParsingException(lineNbr, "Cannot open URL", e);
	    } catch (IOException e) {
	        log.info("File {} not found", m3uFileOnDisk);
	        getRemoteM3U = true;
	    }

	    // If the file is outdated, retrieve it from the remote server
	    if (getRemoteM3U) {
	        backupFileIfNeeded(m3uFileOnDisk);
	        log.info("Retrieving {} from remote server", m3uFileOnDisk);
	        Utils.copyUrlToFileUsingCommonsIO(dp.getStreamChannels(), m3uFileOnDisk.toString());
	    }

	    // Read and parse the file
	    try (BufferedReader buffer = Files.newBufferedReader(m3uFileOnDisk.toPath())) {
	        line = buffer.readLine();
	        if (line == null || line.trim().isEmpty()) {
	            throw new ParsingException(0, "Empty stream");
	        }

	        // Ensure the file starts with the correct marker
	        checkStart(line);

	        // Initialize the pattern matcher and included countries
	        PatternMatcher patternMatcher = PatternMatcher.getInstance();
	        M3UItem entry = null;
	        String[] includedCountries = dp.getIncludedCountries();

	        // Process each line
	        while ((line = buffer.readLine()) != null) {
	            lineNbr++;
	            if (isExtInfo(line)) {
	                entry = extractExtInfo(patternMatcher, line, includedCountries);
	            } else if (entry != null) {
	                // Process channel URL and other related information
	                processChannelLine(line, entry, dp);
	                entries.add(entry);
	            }
	        }
	    } catch (IOException e) {
	        restoreFile(m3uFileOnDisk);
	        throw new ParsingException(lineNbr, "Cannot read file", e);
	    }

	    sw.stop();
	    log.info("Total time for parsing: {} ms", sw.getTotalTimeMillis());
	    return entries;
	}

	/**
	 * Backup the M3U file if it exists before updating or fetching a new version.
	 */
	private static void backupFileIfNeeded(File m3uFileOnDisk) {
	    if (m3uFileOnDisk.exists()) {
	        FileUtilsForM3UToolsJPA.backupFile(m3uFileOnDisk.toString());
	    }
	}

	/**
	 * Restore the M3U file in case of an error during the parsing process.
	 */
	private static void restoreFile(File m3uFileOnDisk) {
	    FileUtilsForM3UToolsJPA.restoreFile(m3uFileOnDisk.toString());
	}

	/**
	 * Process the channel line by deriving the type and associating it with the M3U item.
	 */
	private static void processChannelLine(String line, M3UItem entry, DownloadProperties dp) {
	    String type = Utils.deriveGroupTypeByUrl(line);
	    entry.setType(type);
	    entry.setChannelUri(line);
	    
	    // Clean country identifiers from the channel name and TVG name
	    entry.setChannelName(RegexUtils.removeCountryIdentifier(entry.getChannelName(), dp.getIncludedCountries()));
	    entry.setTvgName(RegexUtils.removeCountryIdentifier(entry.getTvgName(), dp.getIncludedCountries()));
	}

	/**
	 * Ensure the file starts with the #EXTM3U marker.
	 */
	private static void checkStart(String line) {
	    if (line != null && !line.contains(Patterns.M3U_START_MARKER)) {
	        throw new ParsingException(1, "First line of the file should be " + Patterns.M3U_START_MARKER);
	    }
	}

	
	private static boolean isExtInfo(String line) {
		return line.contains(Patterns.M3U_INFO_MARKER);
	}

	private static M3UItem extractExtInfo(PatternMatcher patternMatcher, String line, String[] includedCountries) {
	    // Get the download properties instance to access country-specific regex
	    DownloadProperties dp = DownloadProperties.getInstance();
	    
	    // Extract TVG name and check for invalid or empty cases
	    String tvgName = patternMatcher.extract(line, Patterns.TVG_NAME_REGEX);
	    if (tvgName == null || tvgName.startsWith("#####") || tvgName.isEmpty()) {
	        return null; // Invalid TVG name, return null
	    }
	    
	    // Check if TVG name matches any of the included countries
	    int countryIndex = Utils.indexOfAny(tvgName.substring(0, 3), includedCountries);
	    if (countryIndex == -1) {
	        return null; // No matching country, return null
	    }

	    // Clean up TVG name (remove country identifier and any unwanted text)
	    tvgName = StringUtils.removeCountryIdentifierUsingRegExpr(tvgName, dp.getCountryRegExpr());
	    tvgName = StringUtils.cleanTextContent(tvgName);

	    // Extract other fields from the line
	    String channelName = patternMatcher.extract(line, Patterns.CHANNEL_NAME_REGEX);
	    channelName = (channelName != null) ? StringUtils.removeCountryIdentifierUsingRegExpr(channelName, dp.getCountryRegExpr()) : "";
	    channelName = StringUtils.cleanTextContent(channelName);

	    // Extract additional info
	    String duration = patternMatcher.extract(line, Patterns.DURATION_REGEX);
	    String tvgId = patternMatcher.extract(line, Patterns.TVG_ID_REGEX);
	    String tvgShift = patternMatcher.extract(line, Patterns.TVG_SHIFT_REGEX);
	    String radio = patternMatcher.extract(line, Patterns.RADIO_REGEX);
	    String tvgLogo = patternMatcher.extract(line, Patterns.TVG_LOGO_REGEX);
	    String groupTitle = patternMatcher.extract(line, Patterns.GROUP_TITLE_REGEX);

	    // Default groupId is -1L as a placeholder if not found
	    Long groupId = -1L;

	    // Return the M3U item populated with the extracted details
	    return new M3UItem(
	            duration,          // Duration
	            groupTitle,        // Group title (may be null)
	            groupId,           // Group ID (default -1L)
	            tvgId,             // TVG ID
	            tvgName,           // TVG Name (cleaned)
	            "",                // Empty placeholder (optional field)
	            tvgLogo,           // TVG Logo URL
	            tvgShift,          // TVG Shift
	            radio,             // Radio info
	            "",                // Empty placeholder (optional field)
	            channelName,       // Channel name (cleaned)
	            "",                // Empty placeholder (optional field)
	            channelName,       // Channel name again (probably for fallback)
	            false);            // False for the 'isFavorite' field (assuming this is a flag)
	}
}
