package com.hawkins.m3utoolsjpa.utils;

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
import java.util.Date;
import java.util.Properties;

import org.apache.commons.io.FileUtils;

import com.hawkins.m3utoolsjpa.properties.ConfigProperty;
import com.hawkins.m3utoolsjpa.properties.DownloadProperties;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class Utils {
	private static String propertyFile;

	public static File getPropertyFile(String propertyType) {

		String userHome = System.getProperty("user.home");

		if (userHome.charAt(userHome.length() - 1) != File.separatorChar) {
			userHome += File.separator;
		}

		if (log.isDebugEnabled()) {
			log.debug("Utils.readProperties :: Looking for {}videoDownloader/.dmanager/{}", userHome, propertyType);
		}

		File configFile = new File(userHome, "videoDownloader/.dmanager/" + propertyType);

		if (!configFile.exists() && log.isDebugEnabled()) {
			log.debug("{} does not exist", propertyType);
		}

		return configFile;

	}

	public static Properties readProperties(String propertyType) {

		long start = System.currentTimeMillis();

		File configFile = getPropertyFile(propertyType);

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

	public static Properties saveProperties(ConfigProperty configProperty) {
		try (OutputStream output = new FileOutputStream(Utils.getPropertyFile(Constants.CONFIGPROPERTIES))) {

			Properties props = DownloadProperties.getInstance().getProps();

			props.forEach((key, value) -> {
				if (key.equals(configProperty.getKey())) {
					props.replace(configProperty.getKey(), configProperty.getValue());
				}
			});

			props.store(output, "store to properties file");

		} catch (IOException io) {
			if (log.isDebugEnabled()) {
				log.debug(io.getMessage());
			}
		}

		return readProperties(propertyFile);
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
		return String.format("%." + digits + "f", bytes) + " " + dictionary[index];
	}

	public static String printNow() {

		SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
		Date date = new Date();
		return formatter.format(date);
	}
}
