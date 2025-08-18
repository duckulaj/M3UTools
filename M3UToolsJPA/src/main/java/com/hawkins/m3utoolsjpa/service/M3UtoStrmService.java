package com.hawkins.m3utoolsjpa.service;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.codahale.metrics.Counter;
import com.hawkins.m3utoolsjpa.annotations.TrackExecutionTime;
import com.hawkins.m3utoolsjpa.data.M3UItem;
import com.hawkins.m3utoolsjpa.properties.DownloadProperties;
import com.hawkins.m3utoolsjpa.regex.Patterns;
import com.hawkins.m3utoolsjpa.regex.RegexUtils;
import com.hawkins.m3utoolsjpa.utils.Constants;
import com.hawkins.m3utoolsjpa.utils.Utils;
import com.hawkins.m3utoolsjpa.redis.M3UItemRedisService;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class M3UtoStrmService {

    @Autowired
    M3UService m3uService;
    @Autowired
    private M3UItemRedisService m3UItemRedisService;

    private static final DownloadProperties dp = DownloadProperties.getInstance();
    private static final Pattern SEASON_PATTERN = Pattern.compile("[S]{1}[0-9]{2}", Pattern.CASE_INSENSITIVE);

    @TrackExecutionTime
    public void convertM3UtoStream() {
        List<M3UItem> allItems = getAllM3UItemsFromCache();
        List<M3UItem> movies = filterItems(allItems, ofType(Constants.MOVIE));
        log.info("{} Movies", movies.size());
        createMovieFolders(movies);
        log.info("Created HD Movies folders");

        List<M3UItem> tvshows = filterItems(allItems, ofType(Constants.SERIES));
        log.info("{} TV Shows", tvshows.size());
        createTVshowFolders(tvshows);
        log.info("Created TV Shows folders");
    }

    private List<M3UItem> getAllM3UItemsFromCache() {
        // Now fetch all items from Redis cache
        List<M3UItem> cachedItems = m3UItemRedisService.findAll();
        if (cachedItems != null && !cachedItems.isEmpty()) {
            log.info("Reading from cache");
            return cachedItems;
        }
        // Fallback to DB if cache is empty
        return m3uService.getM3UItems();
    }

    public static Predicate<M3UItem> ofType(String type) {
        return p -> p.getType().equals(type);
    }

    public static Predicate<M3UItem> ofTypeDefinition(String definition) {
        return p -> p.getChannelName().contains(definition) && p.getType().equalsIgnoreCase(Constants.MOVIE);
    }

    public static List<M3UItem> filterItems(List<M3UItem> items, Predicate<M3UItem> predicate) {
        return items.stream().filter(predicate).collect(Collectors.toList());
    }

    private void createMovieFolders(List<M3UItem> movies) {
        log.debug("Starting createMovieFolders");
        log.debug("Processing {} movies", movies.size());
        makeMovieFolders(movies);
    }

    private void createTVshowFolders(List<M3UItem> tvshows) {
        Counter numberOfNewTVShows = new Counter();
        Counter numberOfExistingTVShows = new Counter();

        log.debug("Starting createTVshowFolders");
        String tvShowFolder = createFolder(Constants.FOLDER_TVSHOWS) + File.separator;
        log.debug("Processing {} TV Shows", tvshows.size());

        tvshows.forEach(tvShow -> {
            int endIndex = StringUtils.indexOfAny(tvShow.getGroupTitle(), dp.getIncludedCountries());
            if (endIndex != -1) {
                String tvShowName = cleanTVShowName(tvShow.getChannelName());
                Matcher seasonMatcher = SEASON_PATTERN.matcher(tvShowName);
                if (seasonMatcher.find()) {
                    String season = seasonMatcher.group();
                    int seasonStartIndex = seasonMatcher.start();
                    String folder = tvShowName.substring(0, seasonStartIndex - 1).trim();
                    File tvShowSeasonFolder = new File(tvShowFolder + folder);
                    if (!tvShowSeasonFolder.exists()) tvShowSeasonFolder.mkdir();
                    File seasonFolder = new File(tvShowSeasonFolder.getAbsolutePath() + File.separator + season);
                    if (!seasonFolder.exists()) seasonFolder.mkdir();
                    File thisFile = new File(seasonFolder.getAbsolutePath() + File.separator + tvShowName + ".strm");
                    if (!thisFile.exists()) {
                        try {
                            Utils.writeToFile(thisFile, tvShow.getChannelUri());
                            numberOfNewTVShows.inc();
                        } catch (IOException e) {
                            log.error("Error writing to file", e);
                        }
                    } else {
                        numberOfExistingTVShows.inc();
                    }
                }
            }
        });

        log.info("Number of existing TV Show episodes = {}", numberOfExistingTVShows.getCount());
        log.info("Number of new TV Show episodes = {}", numberOfNewTVShows.getCount());
    }

    private static String createFolder(String folder) {
        String baseDirectory = dp.getDownloadPath() + File.separator;
        File newDirectory = new File(baseDirectory + folder);
        if (!newDirectory.exists()) newDirectory.mkdir();
        log.debug("Created folder {}", newDirectory.getAbsolutePath());
        return newDirectory.getAbsolutePath();
    }

    private static void makeMovieFolders(List<M3UItem> movies) {
        String movieFolder = createFolder(Constants.FOLDER_MOVIES) + File.separator;
        Counter numberOfNewMovies = new Counter();
        Counter numberOfExistingMovies = new Counter();
        log.debug("Created {}", movieFolder);

        movies.forEach(movie -> {
            int endIndex = StringUtils.indexOfAny(movie.getGroupTitle(), dp.getIncludedCountries());
            if (endIndex != -1) {
                String folder = cleanMovieName(movie.getChannelName());
                String url = movie.getChannelUri();
                try {
                    String newFolderPath = createFolder(Constants.FOLDER_MOVIES + File.separator + folder);
                    log.debug("Created {}", newFolderPath);
                    File thisFile = new File(newFolderPath + File.separator + folder + ".strm");
                    if (!thisFile.exists()) {
                        Utils.writeToFile(thisFile, url);
                        numberOfNewMovies.inc();
                    } else {
                        numberOfExistingMovies.inc();
                    }
                } catch (IOException ioe) {
                    log.error("Error creating movie folder", ioe);
                }
            }
        });

        log.info("Number of existing Movies = {}", numberOfExistingMovies.getCount());
        log.info("Number of new Movies = {}", numberOfNewMovies.getCount());
    }
    
    private static String cleanTVShowName(String name) {
    	return cleanName(name);    }

    private static String cleanMovieName(String name) {
        return cleanName(name);
    }
    
    private static String cleanName(String name) {
        name = Utils.replaceForwardSlashWithSpace(name);
        name = Utils.removeFromString(name, Patterns.SQUARE_BRACKET_REGEX);
        name = Utils.removeFromString(name, Patterns.PIPES_REGEX);
        name = Utils.removeFromString(name, Patterns.PIPE_REGEX);
        name = Utils.removeFromString(name, Patterns.HYPHEN_REGEX);
        return RegexUtils.removeCountryIdentifier(name, dp.getIncludedCountries());
    }
}