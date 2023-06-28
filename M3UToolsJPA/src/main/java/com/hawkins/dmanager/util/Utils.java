package com.hawkins.dmanager.util;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Properties;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
// import org.apache.tomcat.util.http.fileupload.FileUtils;

import com.hawkins.m3utoolsjpa.data.M3UItem;
import com.hawkins.m3utoolsjpa.data.M3UItemRepository;
import com.hawkins.m3utoolsjpa.jobs.DownloadJob;
import com.hawkins.m3utoolsjpa.utils.Constants;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class Utils {
	private static String propertyFile;

	public static Properties readProperties(String propertyType) {

		long start = System.currentTimeMillis();

		String userHome = System.getProperty("user.home");

		if(userHome.charAt(userHome.length()-1)!=File.separatorChar){
			userHome += File.separator;
		}

		if (log.isDebugEnabled()) {
			log.debug("Utils.readProperties :: Looking for {}videoDownloader/.dmanager/{}", userHome, propertyType);
		}

		File configFile = new File(userHome, "videoDownloader/.dmanager/" + propertyType);

		if (!configFile.exists() && log.isDebugEnabled()) {
			log.debug("{} does not exist", propertyType);
		}

		Properties props = new Properties();

		try {
			FileReader reader = new FileReader(configFile);
			props.load(reader);
			reader.close();
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

	public static Properties saveProperties(List<String> newProperties) {
		try (OutputStream output = new FileOutputStream(Constants.CONFIGPROPERTIES)) {

			Properties prop = new Properties();

			// set the properties value
			prop.setProperty("channels", newProperties.get(0));
			prop.setProperty("fullM3U", newProperties.get(1));
			prop.setProperty("downloadPath", newProperties.get(2));
			prop.setProperty("moviedb.searchURL", newProperties.get(3));
			prop.setProperty("moviedb.apikey", newProperties.get(4));
			prop.setProperty("moviedb.searchMovieURL", newProperties.get(5));

			// save properties to project root folder
			prop.store(output, null);

			if (log.isDebugEnabled()) {
				log.debug(prop.toString());
			}

		} catch (IOException io) {
			if (log.isDebugEnabled()) {
				log.debug(io.getMessage());
			}
		}

		return readProperties(propertyFile);
	}


	public static File copyUrlToFile(String url, String fileName) {
		long start = System.currentTimeMillis();

		try (
				FileOutputStream fileOS = new FileOutputStream(fileName);
				ReadableByteChannel readChannel = Channels.newChannel((new URL(url)).openStream());	
				){

			fileOS.getChannel().transferFrom(readChannel, 0L, Long.MAX_VALUE);

			long end = System.currentTimeMillis();
			if (log.isDebugEnabled()) {
				log.debug("copyUrlToFile executed in {} ms", (end - start));
			}
			
			return new File(fileName);
		} catch (IOException ioe) {
			if (log.isDebugEnabled()) {
				log.debug(ioe.getMessage());
			}
		}
		return new File(fileName); 

		
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

	public static URL getFinalLocation(String address) throws IOException{

		long start = System.currentTimeMillis();
		String originalURL = address;

		URL url = new URL(address);
		HttpURLConnection conn = (HttpURLConnection)url.openConnection();
		int status = conn.getResponseCode();
		if (status != HttpURLConnection.HTTP_OK) 
		{
			if (status == HttpURLConnection.HTTP_MOVED_TEMP
					|| status == HttpURLConnection.HTTP_MOVED_PERM
					|| status == HttpURLConnection.HTTP_SEE_OTHER)
			{
				String newLocation = conn.getHeaderField("Location");
				return getFinalLocation(newLocation);
			}
		}

		if (!originalURL.equalsIgnoreCase(address)) log.debug("Final URL is different to Original URL");

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

	public static String getURLFromName(String filmName, M3UItemRepository m3uItemRepository) {

		String url = null;

		List<M3UItem> filmList = m3uItemRepository.findByTvgName(filmName);
		

		ListIterator<M3UItem> iFilters = filmList.listIterator();

		while (iFilters.hasNext()) {
			M3UItem m3uItem = iFilters.next();

			if (m3uItem.getTvgName().equalsIgnoreCase(filmName)) {
				url = m3uItem.getChannelUri();
				break;
			}

		}

		return url;

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

	public static LinkedList<DownloadJob> removeJobs(LinkedList<DownloadJob> jobs) {

		LinkedList<DownloadJob> runningJobs = new LinkedList<>();

		ListIterator<DownloadJob> iJobs = jobs.listIterator();

		while (iJobs.hasNext()) {
			DownloadJob j = iJobs.next();
			if (!j.getState().equalsIgnoreCase(Constants.CANCELLED)) {
				runningJobs.add(j);
			} else {
				try {
					FileUtils.forceDelete(new File(j.getDestination()));
				} catch (IOException e) {
					if (log.isDebugEnabled()) {
						log.debug(e.getMessage());
					}
				}
			}

		}

		return runningJobs;
	}


	public static String getFileExtension(String url) {

		return url.substring(url.length() - 3);

	}

	public static boolean containsIgnoreCase(String str, String subString) {
		return str.toLowerCase().contains(subString.toLowerCase());
	}

	public long getRandomLong() {
		long leftLimit = 1L;
		long rightLimit = 10L;

		return leftLimit + (long) (Math.random() * (rightLimit - leftLimit));

	}

	
	public static String replaceForwardSlashWithSpace (String stringToReplace) {


		if (stringToReplace != null) {
			if (stringToReplace.contains("/")) {
				stringToReplace = stringToReplace.replace("/", " ");
			}
		}

		return stringToReplace;
	}

	public static boolean containsWords(String inputString, String[] words) {
		List<String> inputStringList = Arrays.asList(inputString.split(" "));
		List<String> wordsList = Arrays.asList(words);

		return wordsList.stream().anyMatch(inputStringList::contains);
	}

	public static String replaceAndStrip(String thisString, String[] wordsToStrip) {

		int wordsToStripCount = wordsToStrip.length;

		String[] spaces = new String[wordsToStripCount];

		for (int i = 0; i < spaces.length; i++) {
			spaces[i] = "";
		}
		return StringUtils.replaceEach(thisString, wordsToStrip, spaces);


	}

	public static String printNow() {

		SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");  
		Date date = new Date();  
		return formatter.format(date);  
	}
	
	public static File downloadFile(String src, String fileName) {
		
		URL url;
		try {
			url = new URL(src);
			ReadableByteChannel readableByteChannel = Channels.newChannel(url.openStream());
			FileOutputStream fileOutputStream = new FileOutputStream(fileName);
			
			try {
				fileOutputStream.getChannel().transferFrom(readableByteChannel, 0, Long.MAX_VALUE);
			} catch(Exception e) {
				log.info(e.getMessage());
			} finally {
				fileOutputStream.close();
			}
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
		return new File(fileName);
		
	}
}

