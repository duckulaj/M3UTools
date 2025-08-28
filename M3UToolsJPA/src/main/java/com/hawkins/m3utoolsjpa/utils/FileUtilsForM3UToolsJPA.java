package com.hawkins.m3utoolsjpa.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.input.ReversedLinesFileReader;
import org.springframework.beans.factory.annotation.Autowired;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.hawkins.m3utoolsjpa.annotations.TrackExecutionTime;
import com.hawkins.m3utoolsjpa.exception.DownloadFailureException;
import com.hawkins.m3utoolsjpa.properties.DownloadProperties;
import com.hawkins.m3utoolsjpa.service.XtreamService;
import com.hawkins.m3utoolsjpa.xtream.XtreamCodes;

import lombok.extern.slf4j.Slf4j;


@Slf4j
public class FileUtilsForM3UToolsJPA {

	private static final XmlMapper XML_MAPPER = new XmlMapper();
	private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
	static DownloadProperties dp = DownloadProperties.getInstance();
	
	private static final String API_URL = dp.getxTreamUrl();
	private static final String USERNAME = dp.getxTreamUser();
	private static final String PASSWORD = dp.getxTreamPassword();
	
	@Autowired
	static XtreamService xtreamService;

	private FileUtilsForM3UToolsJPA() {
	}

	public static String fileTail(String filename, int linecount) {
		StringBuilder sb = new StringBuilder();
		String lineSeparator = System.lineSeparator();

		File file = new File(filename);
		if (!file.exists()) {
			return "Log file not found";
		}

		int linesInFile = numberOfLines(file);
		if (linecount > linesInFile) {
			linecount = linesInFile;
		}

		try (ReversedLinesFileReader object = ReversedLinesFileReader.builder()
				.setPath(file.getPath())
				.setBufferSize(4096)
				.setCharset(StandardCharsets.UTF_8)
				.get()) {

			for (int counter = 0; counter < linecount; counter++) {
				sb.append(object.readLine()).append(lineSeparator);
			}
		} catch (IOException e) {
			log.error("Error reading file", e);
		}

		return sb.toString();
	}

	public static int numberOfLines(File thisFile) {
		try (BufferedReader br = new BufferedReader(new FileReader(thisFile))) {
			return (int) br.lines().count();
		} catch (IOException e) {
			log.error("Error counting lines in file", e);
		}
		return 0;
	}

	public static String getCurrentWorkingDirectory() {
		return Paths.get("").toAbsolutePath().toString() + File.separator;
	}

	public static void backupFile(String fileName) {
		File file = new File(fileName);
		if (file.exists()) {
			File backupFile = new File(fileName + ".bu");
			try {
				FileUtils.copyFile(file, backupFile);
			} catch (IOException e) {
				log.debug("Error creating {}", backupFile.toString(), e);
			}
		}
	}

	public static void restoreFile(String fileName) {
		File backupFile = new File(fileName + ".bu");
		File file = new File(fileName);
		if (backupFile.exists()) {
			try {
				FileUtils.copyFile(backupFile, file);
			} catch (IOException e) {
				log.debug("Error creating {}", file.toString(), e);
			}
		}
	}

	public static void xmlToJsonConverter() {
		File xmlFile = new File("./generatedChannels.xml");
		if (!xmlFile.exists()) {
			log.error("XML file not found: {}", xmlFile.getAbsolutePath());
			return;
		}

		try {
			JsonNode jsonNode = XML_MAPPER.readTree(xmlFile);
			String jsonString = OBJECT_MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(jsonNode);
			Utils.writeToFile(new File("./epg.json"), jsonString);
		} catch (IOException e) {
			log.error("Error converting XML to JSON", e);
		}
	}

	@TrackExecutionTime
	public static void getM3UFile(File m3uFileOnDisk) throws DownloadFailureException {

		boolean getRemoteM3U = false;

		DownloadProperties dp = DownloadProperties.getInstance();

		try {

			getRemoteM3U = Utils.fileOlderThan(m3uFileOnDisk, dp.getFileAgeM3U());

		} catch (IOException e) {
			log.info("File {} not found", m3uFileOnDisk.toString());
			getRemoteM3U = true;
		}

		if (getRemoteM3U) {

			if (m3uFileOnDisk.exists()) FileUtilsForM3UToolsJPA.backupFile(m3uFileOnDisk.toString());

			String streamChannels = dp.getStreamChannels();
			log.info("Retrieving {} from remote server", m3uFileOnDisk.toString());
			try {
				try {
					if (dp.getxTreamUrl() != null && !dp.getxTreamUrl().isEmpty()) {
						xtreamService = new XtreamService();
						
					} else {
						FileDownloader.downloadFileInSegments(dp.getStreamChannels(), m3uFileOnDisk.toString(), dp.getBufferSize());
					}
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
				
				
				
			 } catch (IOException e) {
				log.info("Error in parse: " + e.getMessage());
				throw new DownloadFailureException("Failed to download M3U file", e);
			} 
		}
	}

	
}