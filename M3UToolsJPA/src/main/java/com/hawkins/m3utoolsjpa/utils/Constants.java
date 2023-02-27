package com.hawkins.m3utoolsjpa.utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Constants {
	
	public Constants() {
		
	}

	public static final String CANCELLED = "CANCELLED";
	public static final String RUNNING = "RUNNING";
	public static final String NEW = "NEW";
	public static final String PAUSED = "PAUSED";
	public static final String STAT_PAUSED = "Paused";
	public static final String STAT_FINISHED = "Finished";
	public static final String STAT_DOWNLOADING = "Downloading";
	public static final String STAT_ASSEMBLING = "Assembling";
	
	public static final String CONFIGPROPERTIES = "config.properties";
	public static final String CONFIGPROPERTIES_BU = "config.properties.bu";
	public static final String DMPROPERTIES = "dm.properties";
	
	public static final String AVI = "avi";
	public static final String MKV = "mkv";
	public static final String MP4 = "mp4";
	
	public static final String LIVE = "live";
	public static final String TVSHOW = "tvshow";
	public static final String MOVIE = "movie";
	public static final String SERIES = "series";
	
	
	public static final String GROUPS = "groups";
	public static final String SELECTEDGROUP = "selectedGroup";
	public static final String SEARCHFILTER = "searchFilter";
	public static final String MOVIEDB = "movieDb";
	public static final String FILMS = "films";
	public static final String TV_SHOWS = "tvshows";
	public static final String JOBLIST = "jobList";
	public static final String SEARCHYEAR = "searchYear";
	public static final String SETTINGS = "settings";
	public static final String ROWS = "rows";
	public static final String EDITM3U = "editM3U";
	
	// Thymeleaf Pages
	public static final String ITEMS = "items";
	public static final String VIEW_LOG = "viewLog";
	public static final String PROPERTIES= "properties";
	public static final String STATUS = "status";
	public static final String SEARCH = "search";
	public static final String INDEX = "index";
	public static final String TV_CHANNELS = "tvChannels";
	public static final String FILTERS = "filters";
	public static final String ADD_FILTER = "addFilter";
	public static final String EDIT_CHANNELS = "editChannelsForm";
	
	public static final String FOLDER_MOVIES = "Movies";
	public static final String FOLDER_TVSHOWS = "TVshows";
	
	public static final String UHD = "UHD";
	public static final String FHD = "FHD";
	public static final String SD = "SD";
	public static final String HD = "HD";
	public static final String DEFINITION_4K = "[4K]";
	public static final String DEFINITION_8K = "[8K]";
	
	public static final String FOR_ADULTS = "For Adults";
	public static final String ADULT = "ADULT";
	
	public static final String[] allowedExtensions = {"mp4","ts"};
	public static final List<String> toBeConverted = new ArrayList<String>(Arrays.asList(".ts"));
	
	public static final String ACTOR_SEARCH = "actor";
	public static final String YEAR_SEARCH = "year";
	public static final String TITLE_SEARCH = "title";

	public static final String STREAM_PLAYLIST = "stream.playlist";
	public static final String STREAM_CHANNELS = "stream.channels";
	
	// public static final Long maxFileSize = 2147483648L;
	public static final Long maxFileSize = 1073741824L;
	
	
}
