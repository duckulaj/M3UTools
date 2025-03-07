package com.hawkins.m3utoolsjpa.parser;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.LinkedList;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StopWatch;

import com.hawkins.m3utoolsjpa.data.M3UItem;
import com.hawkins.m3utoolsjpa.properties.DownloadProperties;
import com.hawkins.m3utoolsjpa.regex.PatternMatcher;
import com.hawkins.m3utoolsjpa.regex.Patterns;
import com.hawkins.m3utoolsjpa.regex.RegexUtils;
import com.hawkins.m3utoolsjpa.service.ParserUtilsService;
import com.hawkins.m3utoolsjpa.utils.Constants;
import com.hawkins.m3utoolsjpa.utils.FileUtilsForM3UToolsJPA;
import com.hawkins.m3utoolsjpa.utils.StringUtils;
import com.hawkins.m3utoolsjpa.utils.Utils;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class Parser {

	@Autowired
	static ParserUtilsService pu;
	
    public static LinkedList<M3UItem> parse() {
        StopWatch sw = new StopWatch();
        sw.start("parse");

        int lineNbr = 0;
        LinkedList<M3UItem> entries = new LinkedList<>();
        DownloadProperties dp = DownloadProperties.getInstance();
        String[] includedCountries = dp.getIncludedCountries();

        File m3uFileOnDisk = new File(Constants.M3U_FILE);

        FileUtilsForM3UToolsJPA.getM3UFile(m3uFileOnDisk);

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
        if (groupTitle == null || !pu.isIncludedCountry(includedCountries, groupTitle)) return null;

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
