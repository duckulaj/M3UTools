package com.hawkins.m3utoolsjpa.service;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.FileSystemUtils;

import com.hawkins.m3utoolsjpa.data.M3UItem;
import com.hawkins.m3utoolsjpa.properties.DownloadProperties;
import com.hawkins.m3utoolsjpa.regex.Patterns;
import com.hawkins.m3utoolsjpa.regex.RegexUtils;
import com.hawkins.m3utoolsjpa.utils.Constants;
import com.hawkins.m3utoolsjpa.utils.Utils;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class M3UtoStrmService {

	@Autowired
	M3UService m3uService;
	
	private static String[] videoTypes = {Constants.AVI, Constants.MKV, Constants.MP4};
	// private static String[] viewingDefinitions = {"[SD]", "[FHD]", "[UHD]", "[HD]", "[4K]", "[8K]"};
	private static String tvShowRegex = "[S]{1}[0-9]{2} [E]{1}[0-9]{2}";
	private static String seasonRegex = "[S]{1}[0-9]{2}";
	
	public void convertM3UtoStream() {

		/*
		 * 1. Get an instance of the group list
		 * 2. For each group decide if it is TV VOD or Film VOD
		 * 3. For films we need to create a folder for each film
		 * 4. In each folder write out the link to a strm file
		 * 5. For TV Shows create a folder
		 * 6. Create a subfolder for each Season
		 * 7. Create a an strm file for each episode within a season
		 */

		CompletableFuture<Void> createMovies = CompletableFuture.runAsync(() -> {
			List<M3UItem> movies = m3uService.getM3UItemsByType(Constants.MOVIE);
			log.info("{} Movies", movies.size());
			
			createMovieFolders(movies);
			log.info("Created HD Movies folders");
		});
		
		CompletableFuture<Void> createTVShows = CompletableFuture.runAsync(() -> {
			List<M3UItem> tvshows = m3uService.getM3UItemsByType(Constants.SERIES);
			log.info("{} TV Shows", tvshows.size());
			
			createTVshowFolders(tvshows);
			log.info("Created TV Shows folders");
		});

				
	}


	public static String deriveStreamType (M3UItem item) {

		String streamType = null;
		String videoExtension = null;
		String stream = item.getChannelUri();
		String name = item.getChannelName();

		
		if (item.getType().equalsIgnoreCase(Constants.LIVE) || item.getType().equals("") ) {

			streamType = Constants.LIVE;

		} else {
			// Get the last three characters from the stream

			// TO-DO: The following line needs testing, then look at completeable futures for creating folders
			
			if (stream.length() > 3) videoExtension = stream.substring(stream.lastIndexOf(".") + 1);

			if (Arrays.asList(videoTypes).contains(videoExtension)) {

				// Check to see if we have Season and Episode information in the form of S01 E01


				Pattern pattern = Pattern.compile(tvShowRegex, Pattern.CASE_INSENSITIVE);
				Matcher matcher = pattern.matcher(name);
				boolean matchFound = matcher.find();

				if (matchFound) {
					streamType = Constants.TVSHOW;
				} else {
					streamType = Constants.MOVIE;
				}

			} else {
				streamType = Constants.LIVE;
			}

			if (stream.contains(Constants.SERIES)) {
				streamType = Constants.TVSHOW;
			} else if (stream.contains(Constants.MOVIE)) {
				streamType = Constants.MOVIE;
			}
		}
		return streamType;
	}


	public static Predicate<M3UItem> ofType(String type) {
		return p -> p.getType().equals(type);
	}

	public static Predicate<M3UItem> ofTypeDefinition(String definition) {
		return p -> p.getChannelName().indexOf(definition) != -1 && p.getType().equalsIgnoreCase(Constants.MOVIE);
	}

	public static List<M3UItem> filterItems (List<M3UItem> items, Predicate<M3UItem> predicate) {

		return items.stream().filter( predicate ).collect(Collectors.<M3UItem>toList());
	}

	public static void createMovieFolders (List<M3UItem> movies) {

		if (log.isDebugEnabled()) {
			log.debug("Starting createMovieFolders");
		}

		deleteFolder(Constants.FOLDER_MOVIES);

		if (log.isDebugEnabled()) {
			log.debug("Processing {} movies", movies.size());
		}

		if (log.isDebugEnabled()) {
			log.debug("Processing {} movies", movies.size());
		}
		makeMovieFolders(movies);

	}

	public static void createTVshowFolders (List<M3UItem> tvshows) {

		if (log.isDebugEnabled()) {
			log.debug("Starting createTVshowFolders");
		}

		deleteFolder(Constants.FOLDER_TVSHOWS);
		String tvShowFolder = createFolder(Constants.FOLDER_TVSHOWS) + File.separator;

		if (log.isDebugEnabled()) {
			log.debug("Processing {} TV Shows", tvshows.size());
		}

		if (log.isDebugEnabled()) {
			log.debug("Processing {} TV Shows", tvshows.size());
		}

		tvshows.forEach(tvShow -> {
			String tvShowName = tvShow.getChannelName();
			tvShowName = Utils.replaceForwardSlashWithSpace(tvShowName);
			tvShowName = Utils.removeFromString(tvShowName, Patterns.SQUARE_BRACKET_REGEX);
			tvShowName = Utils.removeFromString(tvShowName, Patterns.PIPES_REGEX);
			tvShowName = Utils.removeFromString(tvShowName, Patterns.PIPE_REGEX);
			
			if (tvShowName.contains("EN ")) {
				tvShowName = tvShowName.substring(3);
			}
					
			Pattern seasonPattern = Pattern.compile(seasonRegex, Pattern.CASE_INSENSITIVE);
			Matcher seasonMatcher = seasonPattern.matcher(tvShowName);
			boolean seasonMatchFound = seasonMatcher.find();

			if (seasonMatchFound) {
				String season = seasonMatcher.group();
				int seasonStartIndex = seasonMatcher.start();
				/*
				 * Create the TV Show folder
				 */
				String folder = tvShowName.substring(0, seasonStartIndex - 1).trim();
				File tvShowSeasonFolder = new File(tvShowFolder + folder);
				if (!tvShowSeasonFolder.exists()) tvShowSeasonFolder.mkdir();
				/*
				 * Create the Season folder
				 */
				File seasonFolder = new File(tvShowSeasonFolder.getAbsolutePath() + File.separator + season);
				if (!seasonFolder.exists()) seasonFolder.mkdir();

				File thisFile = new File(seasonFolder.getAbsolutePath() + File.separator + tvShowName + ".strm"); 
				try {
					writeToFile(thisFile, tvShow.getChannelUri());
				} catch (IOException e) {
					e.printStackTrace();
				}
			}

		});
	}

	public static void deleteFolder (String folder) {

		Path pathToBeDeleted = new File(folder).toPath();

		
		try {
			
			FileSystemUtils.deleteRecursively(pathToBeDeleted);
			
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	public static String createFolder (String folder) {

		String baseDirectory = DownloadProperties.getInstance().getDownloadPath() + File.separator;

		File newDirectory = new File(baseDirectory + folder);

		// if (newDirectory.exists()) {
		// 	deleteFolder(newDirectory.getAbsolutePath());
		// }

		newDirectory.mkdir();

		if (log.isDebugEnabled()) {
			log.debug("Created folder {}", newDirectory.getAbsolutePath());
		}

		return newDirectory.getAbsolutePath();

	}

	public static void writeToFile(File thisFile, String content) throws IOException{

		if (log.isDebugEnabled()) {
			log.debug("Writing file {}", thisFile.getAbsolutePath());
		}

		FileWriter writer = new FileWriter(thisFile);
		writer.write(content);

		writer.close();
		
		
	}

	public static void makeMovieFolders(List<M3UItem> movies) {

		String movieFolder = createFolder(Constants.FOLDER_MOVIES) + File.separator;
		
		if (log.isDebugEnabled()) {
			log.debug("Created {}", movieFolder);
		}
		
		movies.forEach(movie -> {

			String groupTitle = movie.getGroupTitle();
			
			String folder = movie.getChannelName();
			folder = Utils.replaceForwardSlashWithSpace(folder);
			folder = Utils.removeFromString(folder, Patterns.SQUARE_BRACKET_REGEX);
			folder = Utils.removeFromString(folder, Patterns.PIPES_REGEX);
			folder = Utils.removeFromString(folder, Patterns.PIPE_REGEX);
			
			folder = folder.replace("/", " ").trim();
			
			if (folder.contains("EN ")) {
				folder = folder.substring(3);
			}

			String url = movie.getChannelUri();

			try {
				if (!RegexUtils.containsRegex(groupTitle, Patterns.ADULT_REGEX)) {

					// String newFolder = Utils.normaliseName(folder);
					String newFolder = folder;
					String newFolderPath = createFolder(Constants.FOLDER_MOVIES + File.separator + newFolder);
					
					if (log.isDebugEnabled()) {
						log.debug("Created {}", newFolderPath);
					}
					
					File thisFile = new File(newFolderPath + File.separator + folder + ".strm"); 
					writeToFile(thisFile, url);
					
					if (log.isDebugEnabled()) {
						log.debug("Written - {}", thisFile.getAbsolutePath());
					}

				}
			} catch (IOException ioe) {
				ioe.getMessage();
			}

		});
	}
}