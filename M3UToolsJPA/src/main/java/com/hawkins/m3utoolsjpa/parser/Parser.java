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
import com.hawkins.m3utoolsjpa.regex.RegexUtils;
import com.hawkins.m3utoolsjpa.utils.Constants;
import com.hawkins.m3utoolsjpa.utils.FileDownloader;
import com.hawkins.m3utoolsjpa.utils.FileUtilsForM3UToolsJPA;
import com.hawkins.m3utoolsjpa.utils.ParserUtils;
import com.hawkins.m3utoolsjpa.utils.StringUtils;
import com.hawkins.m3utoolsjpa.utils.Utils;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class Parser {

    public static LinkedList<M3UItem> parse() {
        StopWatch sw = new StopWatch();
        sw.start("parse");

        int lineNbr = 0;
        LinkedList<M3UItem> entries = new LinkedList<>();
        DownloadProperties dp = DownloadProperties.getInstance();
        String[] includedCountries = dp.getIncludedCountries();

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
            // Utils.copyUrlToFileUsingCommonsIO(dp.getStreamChannels(), m3uFileOnDisk.toString());
            try {
				FileDownloader.downloadFileInSegments(dp.getStreamChannels(), m3uFileOnDisk.toString(), dp.getBufferSize());
			} catch (IOException | InterruptedException e) {
				// TODO Auto-generated catch block
				log.info("Error in parse: "+e.getMessage());
			}
        }

        try (var buffer = Files.newBufferedReader(m3uFileOnDisk.toPath())) {
            String line = buffer.readLine();
            if (line == null || line.trim().isEmpty()) {
                throw new ParsingException(0, "Empty stream");
            }
            lineNbr++;
            checkStart(line);

            PatternMatcher patternMatcher = PatternMatcher.getInstance();
            M3UItem entry = null;

            while ((line = buffer.readLine()) != null) {
                lineNbr++;
                if (isExtInfo(line)) {
                    entry = extractExtInfo(patternMatcher, line, includedCountries);
                } else if (entry != null) {
                    entry.setType(Utils.deriveGroupTypeByUrl(line));
                    entry.setChannelUri(line);
                    entry.setChannelName(RegexUtils.removeCountryIdentifier(entry.getChannelName(), dp.getIncludedCountries()));
                    entry.setTvgName(RegexUtils.removeCountryIdentifier(entry.getTvgName(), dp.getIncludedCountries()));
                    entries.add(entry);
                }
            }
        } catch (IOException e) {
            FileUtilsForM3UToolsJPA.restoreFile(m3uFileOnDisk.toString());
            throw new ParsingException(lineNbr, "Cannot read file", e);
        }

        sw.stop();
        log.info("Total time in milliseconds for parsing: {}", sw.getTotalTimeMillis());
        return entries;
    }

    private static void checkStart(String line) {
        if (line != null && !line.contains(Patterns.M3U_START_MARKER)) {
            throw new ParsingException(1, "First line of the file should be " + Patterns.M3U_START_MARKER);
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
}
