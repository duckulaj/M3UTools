package com.hawkins.m3utoolsjpa.properties;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Properties;

import org.apache.commons.lang3.SystemUtils;

import com.hawkins.m3utoolsjpa.utils.Constants;
import com.hawkins.m3utoolsjpa.utils.Utils;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class DownloadProperties implements Runnable {


	private static DownloadProperties thisInstance = null;
	private Properties props = null;

	private String channels = null;
	private String fullM3U = null;
	private String downloadPath = null;
	private String filter = null;
	private String movieDbAPI = null;
	private String movieDbURL = null;
	private String movieDbDiscoverURL = null;
	private String searchMovieURL = null;
	private String searchPersonURL = null;
	private String resetM3UFileSchedule = null;
	private String createStreamsSchedule = null;
	private String epgFileName = null;
	private String epgTimeDifference = null;
	private String fileWatcherLocation=null;
	private long fileWatcherPollingDuration = 5000L; //default to 5 seconds
	private boolean embyInstalled = false;
	private String embyApi = null;
	private String embyUrl = null;
	private String fileName = null;
	private String streamXMLUrl = null;
	private String streamChannels = null;
	private int fileAgeM3U = 1;
	private int fileAgeEPG = 1;
	private String[] includedCountries = null;

	public DownloadProperties() {

		this.props = Utils.readProperties(Constants.CONFIGPROPERTIES);

		// this.setChannels(props.getProperty("channels"));


		if (SystemUtils.IS_OS_WINDOWS) {
			this.setDownloadPath(System.getProperty("user.home"));
			this.setFullM3U(this.getDownloadPath() + File.separator + "allChannels.m3u");
		} else if (SystemUtils.IS_OS_LINUX) {
			this.setFullM3U(props.getProperty("fullM3U"));
			this.setDownloadPath(props.getProperty("downloadPath"));
		}

		this.setFilter(props.getProperty("filter"));
		this.setMovieDbAPI(props.getProperty("moviedb.apikey"));
		this.setMovieDbURL(props.getProperty("moviedb.searchURL"));
		this.setMovieDbDiscoverURL(props.getProperty("moviedb.discoverURL"));
		this.setSearchMovieURL(props.getProperty("moviedb.searchMovieURL"));
		this.setSearchPersonURL(props.getProperty("moviedb.searchPersonURL"));
		this.setEpgFileName(props.getProperty("epg.filename"));
		this.setEpgTimeDifference(props.getProperty("epg.time.difference"));
		// this.setFileWatcherLocation(props.getProperty("fileWatcher.location"));
		// this.setFileWatcherPollingDuration(Long.parseLong(props.getProperty("filewatcher.pollingDuration")));
		this.setEmbyInstalled(Boolean.parseBoolean(props.getProperty("emby.installed")));
		this.setEmbyApi(props.getProperty("emby.api"));
		this.setEmbyUrl(props.getProperty("emby.url"));
		this.setFileName(props.getProperty("fileName"));
		this.setStreamXMLUrl(props.getProperty(Constants.STREAM_PLAYLIST));
		this.setStreamChannels(props.getProperty(Constants.STREAM_CHANNELS));
		this.setFileAgeEPG(Integer.valueOf(props.getProperty("fileAge.epg")));
		this.setFileAgeM3U(Integer.valueOf(props.getProperty("fileAge.m3u")));
		this.setIncludedCountries(props.getProperty("includedCountries").split(","));
	}

	public static synchronized DownloadProperties getInstance()
	{
		log.debug("Requesting M3UPlayList instance");

		if (DownloadProperties.thisInstance == null)
		{
			DownloadProperties.thisInstance = new DownloadProperties();
		}

		return DownloadProperties.thisInstance;
	}

	public DownloadProperties updateProperty(ConfigProperty configProperty) {

		try {
			Path sourceFile = Utils.getPropertyFile(Constants.CONFIGPROPERTIES).toPath();
			Path targetFile = Utils.getPropertyFile(Constants.CONFIGPROPERTIES_BU).toPath();

			Files.copy(sourceFile, targetFile, StandardCopyOption.REPLACE_EXISTING);

		} catch (IOException ex) {
			if (log.isDebugEnabled()) {
				log.debug("I/O Error when copying file");
			}
		} catch (Exception e) {
			if (log.isDebugEnabled()) {
				log.debug("Exception copying file");
			}
		}

		Utils.saveProperties(configProperty);

		thisInstance = new DownloadProperties();

		return thisInstance;

	}

	@Override
	public void run() {
		throw new UnsupportedOperationException();
	}

	public String getChannels() {
		return channels;
	}

	public void setChannels(String channels) {
		this.channels = channels;
	}

	public String getFullM3U() {
		return fullM3U;
	}

	public void setFullM3U(String fullM3U) {
		this.fullM3U = fullM3U;
	}

	public String getDownloadPath() {
		return downloadPath;
	}

	public void setDownloadPath(String downloadPath) {
		this.downloadPath = downloadPath;
	}

	public String getFilter() {
		return filter;
	}

	public void setFilter(String filter) {
		this.filter = filter;
	}

	public String getMovieDbAPI() {
		return movieDbAPI;
	}

	public String getResetM3UFileSchedule() {
		return resetM3UFileSchedule;
	}

	public void setResetM3UFileSchedule(String resetM3UFileSchedule) {
		this.resetM3UFileSchedule = resetM3UFileSchedule;
	}

	public String getCreateStreamsSchedule() {
		return createStreamsSchedule;
	}

	public void setCreateStreamsSchedule(String createStreamsSchedule) {
		this.createStreamsSchedule = createStreamsSchedule;
	}

	public void setMovieDbAPI(String movieDbAPI) {
		this.movieDbAPI = movieDbAPI;
	}

	public String getMovieDbURL() {
		return movieDbURL;
	}

	public void setMovieDbURL(String movieDbURL) {
		this.movieDbURL = movieDbURL;
	}

	public String getSearchMovieURL() {
		return searchMovieURL;
	}

	public void setSearchMovieURL(String searchMovieURL) {
		this.searchMovieURL = searchMovieURL;
	}

	public String getEpgFileName() {
		return epgFileName;
	}

	public void setEpgFileName(String epgFileName) {
		this.epgFileName = epgFileName;
	}

	public String getFileWatcherLocation() {
		return fileWatcherLocation;
	}

	public void setFileWatcherLocation(String fileWatcherLocation) {
		this.fileWatcherLocation = fileWatcherLocation;
	}

	public long getFileWatcherPollingDuration() {
		return fileWatcherPollingDuration;
	}

	public void setFileWatcherPollingDuration(long fileWatcherPollingDuration) {
		this.fileWatcherPollingDuration = fileWatcherPollingDuration;
	}

	public boolean isEmbyInstalled() {
		return embyInstalled;
	}

	public void setEmbyInstalled(boolean embyInstalled) {
		this.embyInstalled = embyInstalled;
	}

	public String getEmbyApi() {
		return embyApi;
	}

	public void setEmbyApi(String embyApi) {
		this.embyApi = embyApi;
	}

	public String getEmbyUrl() {
		return embyUrl;
	}

	public void setEmbyUrl(String embyUrl) {
		this.embyUrl = embyUrl;
	}

	public String getEpgTimeDifference() {
		return epgTimeDifference;
	}

	public void setEpgTimeDifference(String epgTimeDifference) {
		this.epgTimeDifference = epgTimeDifference;
	}

	public String getSearchPersonURL() {
		return searchPersonURL;
	}

	public void setSearchPersonURL(String searchPersonURL) {
		this.searchPersonURL = searchPersonURL;
	}

	public String getMovieDbDiscoverURL() {
		return movieDbDiscoverURL;
	}

	public void setMovieDbDiscoverURL(String movieDbDiscoverURL) {
		this.movieDbDiscoverURL = movieDbDiscoverURL;
	}

	public Properties getProps() {

		return this.props;

	}

	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	public String getStreamXMLUrl() {
		return streamXMLUrl;
	}

	public void setStreamXMLUrl(String streamXMLUrl) {
		this.streamXMLUrl = streamXMLUrl;
	}

	public String getStreamChannels() {
		return streamChannels;
	}

	public void setStreamChannels(String streamChannels) {
		this.streamChannels = streamChannels;
	}

	public int getFileAgeM3U() {
		return fileAgeM3U;
	}

	public void setFileAgeM3U(int fileAgeM3U) {
		this.fileAgeM3U = fileAgeM3U;
	}

	public int getFileAgeEPG() {
		return fileAgeEPG;
	}

	public void setFileAgeEPG(int fileAgeEPG) {
		this.fileAgeEPG = fileAgeEPG;
	}

	public String[] getIncludedCountries() {
		return includedCountries;
	}

	public void setIncludedCountries(String[] includedCountries) {
		this.includedCountries = includedCountries;
	}




}
