package com.hawkins.m3utoolsjpa.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Properties;
import java.util.regex.Pattern;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.RegExUtils;
import org.apache.commons.lang3.StringUtils;

import com.hawkins.m3utoolsjpa.properties.ConfigProperty;
import com.hawkins.m3utoolsjpa.properties.DownloadProperties;
import com.hawkins.m3uttoolsjpa.jobs.DownloadJob;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class Utils {
	
	public static File getPropertyFile(String propertyType) {

		File configFile = new File(FileUtilsForM3UToolsJPA.getCurrentWorkingDirectory() + propertyType);
		
		log.info("Reading properties from {}", configFile.getAbsolutePath());
		
		if (!configFile.exists() && log.isDebugEnabled()) {
			log.debug(configFile.getAbsolutePath() + " does not exist");
		}

		return configFile;

	}

	public static Properties readProperties(String propertyType) {

		long start = System.currentTimeMillis();

		

		Properties props = new Properties();
		
		try {
			File configFile = getPropertyFile(propertyType);
			
			if (propertyType.equals(Constants.DMPROPERTIES)) {
				props.load(new FileInputStream(configFile));
			} else {
				props.loadFromXML(new FileInputStream(new File(Constants.CONFIGPROPERTIES)));
			}
			
			props.putIfAbsent("fileName", configFile.getPath());
		} catch (FileNotFoundException fnfe) {
			log.debug(fnfe.toString());
		} catch (IOException ioe) {
			log.debug(ioe.toString());
		}

		long end = System.currentTimeMillis();

		if (log.isDebugEnabled()) {
			log.debug("readProperties executed in {} ms", (end - start));
		}
		return props;
	}

	public static Properties saveProperties(ConfigProperty configProperty) {
		try (OutputStream output = new FileOutputStream(Utils.getPropertyFile(Constants.CONFIGPROPERTIES))) {

			Properties props = DownloadProperties.getInstance().getProps();

			props.forEach((key, value) -> {
				if (key.equals(configProperty.getKey())) {
					props.replace(configProperty.getKey(), configProperty.getValue());
				}
			});

			// props.store(output, "");
			props.storeToXML(new FileOutputStream(new File(Constants.CONFIGPROPERTIES)), "");

		} catch (IOException io) {
			if (log.isDebugEnabled()) {
				log.debug(io.getMessage());
			}
		}

		return readProperties(Constants.CONFIGPROPERTIES);
	}

	public static void copyUrlToFile(String url, String fileName) {
		long start = System.currentTimeMillis();

		try (FileOutputStream fileOS = new FileOutputStream(fileName);
				ReadableByteChannel readChannel = Channels.newChannel((new URL(url)).openStream());) {

			fileOS.getChannel().transferFrom(readChannel, 0L, Long.MAX_VALUE);

		} catch (IOException ioe) {
			if (log.isDebugEnabled()) {
				log.debug(ioe.getMessage());
			}
		}

		long end = System.currentTimeMillis();
		if (log.isDebugEnabled()) {
			log.debug("copyUrlToFile executed in {} ms", (end - start));
		}
	}

	public static void copyUrlToFileUsingCommonsIO(String url, String fileName) {

		try {
			FileUtils.copyURLToFile(new URL(url), new File(fileName), 40000, 30000);
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static URL getFinalLocation(String address) throws IOException {

		long start = System.currentTimeMillis();
		String originalURL = address;

		URL url = new URL(address);
		HttpURLConnection conn = (HttpURLConnection) url.openConnection();
		int status = conn.getResponseCode();
		if (status != HttpURLConnection.HTTP_OK) {
			if (status == HttpURLConnection.HTTP_MOVED_TEMP || status == HttpURLConnection.HTTP_MOVED_PERM
					|| status == HttpURLConnection.HTTP_SEE_OTHER) {
				String newLocation = conn.getHeaderField("Location");
				return getFinalLocation(newLocation);
			}
		}

		if (!originalURL.equalsIgnoreCase(address))
			log.debug("Final URL is different to Original URL");

		if (log.isDebugEnabled()) {
			log.debug("getFinalLocation took {} ms", (System.currentTimeMillis() - start));
		}

		return new URL(address);
	}

	public static String format(double bytes, int digits) {
		String[] dictionary = { "bytes", "KB", "MB", "GB", "TB", "PB", "EB", "ZB", "YB" };
		int index = 0;
		for (index = 0; index < dictionary.length; index++) {
			if (bytes < 1024) {
				break;
			}
			bytes = bytes / 1024;
		}
		return ("%." + digits + "f").formatted(bytes) + " " + dictionary[index];
	}

	public static String printNow() {

		SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
		Date date = new Date();
		return formatter.format(date);
	}
	
	public static String replaceForwardSlashWithSpace (String stringToReplace) {


		if (stringToReplace != null) {
			if (stringToReplace.contains("/")) {
				stringToReplace = stringToReplace.replace("/", " ");
			}
		}

		return stringToReplace;
	}
	
	public static String normaliseName(String filmName) {

		int startIndex = 0;

		int endIndex = StringUtils.indexOfAny(filmName, new String[]{"SD", "FHD", "UHD", "HD", "4K"});

		if (endIndex != -1) {
			filmName = filmName.substring(startIndex, endIndex);
		}

		startIndex = filmName.indexOf(':');
		if (startIndex != -1) {
			filmName = filmName.substring(startIndex + 1).trim();
		}

		if (filmName.contains("(MULTISUB)")) {
			return filmName.replace("(MULTISUB)", "").trim();
		} else {
			return filmName.trim();
		}
		
	}
	
	public static String removeFromString(String originalString, Pattern pattern) {
		
		return RegExUtils.removeAll(originalString, pattern);
		
	}
	
	public static String getParamsString(Map<String, String> params) 
			throws UnsupportedEncodingException{
		StringBuilder result = new StringBuilder();

		for (Map.Entry<String, String> entry : params.entrySet()) {
			result.append(URLEncoder.encode(entry.getKey(), "UTF-8"));
			result.append("=");
			result.append(URLEncoder.encode(entry.getValue(), "UTF-8"));
			result.append("&");
		}

		String resultString = result.toString();
		return resultString.length() > 0
				? resultString.substring(0, resultString.length() - 1)
						: resultString;
	}

	public static String deriveGroupTypeByUrl(String url) {

		String groupType = "";

		if (url.contains(Constants.SERIES)) {
			groupType = Constants.SERIES;
		} else if (url.contains(Constants.MOVIE)) {
			groupType = Constants.MOVIE;
		} else if (url.length() > 0) {
			groupType = Constants.LIVE;
		}

		return groupType;
	}
	
	public static DownloadJob findJobByName(List<DownloadJob> jobs, String name) {

		DownloadJob thisJob = null;

		ListIterator<DownloadJob> iJobs = jobs.listIterator();

		while (iJobs.hasNext()) {
			DownloadJob j = iJobs.next();
			if (j.getJobName().equalsIgnoreCase(name)) {
				thisJob = j;
				break;
			}
		}

		return thisJob;

	}

}
