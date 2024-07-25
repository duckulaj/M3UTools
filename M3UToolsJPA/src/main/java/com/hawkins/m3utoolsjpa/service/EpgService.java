package com.hawkins.m3utoolsjpa.service;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.time.LocalTime;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;
import com.hawkins.m3utoolsjpa.data.M3UItem;
import com.hawkins.m3utoolsjpa.data.M3UItemRepository;
import com.hawkins.m3utoolsjpa.emby.EmbyApi;
import com.hawkins.m3utoolsjpa.epg.Channel;
import com.hawkins.m3utoolsjpa.epg.EpgReader;
import com.hawkins.m3utoolsjpa.epg.XmltvChannel;
import com.hawkins.m3utoolsjpa.epg.XmltvDoc;
import com.hawkins.m3utoolsjpa.epg.XmltvIcon;
import com.hawkins.m3utoolsjpa.epg.XmltvProgramme;
import com.hawkins.m3utoolsjpa.epg.XmltvUtils;
import com.hawkins.m3utoolsjpa.epg.XmltvVideo;
import com.hawkins.m3utoolsjpa.properties.DownloadProperties;
import com.hawkins.m3utoolsjpa.utils.Constants;
import com.hawkins.m3utoolsjpa.utils.Utils;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class EpgService {

	@Autowired
	M3UItemRepository itemRepository;

	private DownloadProperties dp = DownloadProperties.getInstance();

	public void readEPG() {

		log.info("readEPG started at {}", Utils.printNow());

		List<XmltvChannel> selectedXmltvChannels = new ArrayList<XmltvChannel>();
		List<XmltvProgramme> selectedXmltvProgrammes = new ArrayList<XmltvProgramme>();
		XmltvDoc selectedXmltvDoc = new XmltvDoc();
		XmltvDoc doc = new XmltvDoc();
		List<M3UItem> m3uItems = new ArrayList<M3UItem>();

		CompletableFuture<XmltvDoc> xmlTvDoc = CompletableFuture.supplyAsync(() -> 

		getXmlTvDoc()

				);

		CompletableFuture<List<M3UItem>> m3UItems = CompletableFuture.supplyAsync(() -> 

		itemRepository.findTvChannelsBySelected(true)

				);

		CompletableFuture<Void> combinedFuture = CompletableFuture.allOf(m3UItems, xmlTvDoc);
		try {
			log.info("Starting combinedFuture.get() at {}", Utils.printNow());
			combinedFuture.get();
		} catch (InterruptedException | ExecutionException e) {
			e.printStackTrace();
		}

		if (combinedFuture.isDone())
			try {
				doc = xmlTvDoc.get();
				m3uItems = m3UItems.get();
				log.info("Number of M3UItems = {}", m3UItems.get().size());
			} catch (InterruptedException | ExecutionException e) {
				e.printStackTrace();
			}

		// Now that we have the XmltvDoc we can extract the channels that we have selected

		List<XmltvChannel> xmltvChannels = doc.getChannels();
		List<XmltvProgramme> xmltvProgrammes = doc.getProgrammes();

		log.info("Found {} selected m3uItems", m3uItems.size());

		if (xmltvChannels != null) {
			log.info("Found {} channels", xmltvChannels.size());
		} else {
			log.info("No channels found");
			xmltvChannels = new ArrayList<XmltvChannel>();
		}

		if (xmltvProgrammes != null) {
			log.info("Found {} programmes", xmltvProgrammes.size());
		} else {
			log.info("No programmes found");
			xmltvProgrammes = new ArrayList<XmltvProgramme>();
		}

		// We need to ensure that the tvg-id in the epg matches the tvg-id for the m3u Item
		for (M3UItem m3uItem : m3uItems) {

			XmltvChannel foundByStream = doc.getChannelsByIdAndName(m3uItem.getTvgId(), m3uItem.getTvgName());

			if (foundByStream != null) selectedXmltvChannels.add(foundByStream);

		}

		log.info("Processing {} selected channels", selectedXmltvChannels.size());

		if (xmltvProgrammes != null) { 
			for (XmltvChannel channel : selectedXmltvChannels) {

				List<XmltvProgramme> foundByStream = doc.getProgrammesById(channel.getId());

				if (foundByStream != null) {
					for (XmltvProgramme xmltvProgramme : foundByStream) { 
						xmltvProgramme.setChannel(channel.getId()); 
						xmltvProgramme.setIcon(new XmltvIcon("", "", "")); xmltvProgramme.setCredits("");
						xmltvProgramme.setVideo(new XmltvVideo("HDTV"));
						xmltvProgramme.setStart(EpgReader.changeLocalTime(xmltvProgramme.getStart().toString()));
						xmltvProgramme.setStop(EpgReader.changeLocalTime(xmltvProgramme.getStop().toString())); 
						selectedXmltvProgrammes.add(xmltvProgramme); 
					} 
				}
			}
		}

		log.info("Found {} programmes", selectedXmltvProgrammes.size());

		selectedXmltvDoc.setGeneratorName(doc.getGeneratorName());
		selectedXmltvDoc.setSourceInfoName(doc.getSourceInfoName());
		selectedXmltvDoc.setChannels(selectedXmltvChannels);
		selectedXmltvDoc.setProgrammes(selectedXmltvProgrammes);

		try {
			XmlMapper xm = XmltvUtils.createMapper();
			xm.writerWithDefaultPrettyPrinter();
			xm.writeValue(new File("./generatedChannels.xml"), selectedXmltvDoc);
			log.info("Written ./generatedChannels.xml");

			if (selectedXmltvChannels != null && !selectedXmltvChannels.isEmpty()) {
				writeJson(selectedXmltvChannels, doc);
			}

			if (dp.isEmbyInstalled()) {
				EmbyApi.refreshGuide();
			}

		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			log.info("readEPG finished at {}", Utils.printNow());
		}
	}


	public XmltvDoc getXmlTvDoc() {

		// Make the call to this a CompletableFuture

		String epgFile = Constants.EPG_XML;

		XmlMapper xm = XmltvUtils.createMapper();
		XmltvDoc doc = new XmltvDoc();

		File epgFileOnDisk = new File(epgFile);

		boolean getRemoteEPG = false;
		try {
			getRemoteEPG = Utils.fileOlderThan(epgFileOnDisk, dp.getFileAgeEPG());
		} catch (IOException ex) {
			log.info("File {} does not exist", epgFile);
			getRemoteEPG = true;
		}

		try {
			if (getRemoteEPG) {

				String url = dp.getStreamXMLUrl();
				log.info("Retrieving EPG from remote server");
				Utils.copyUrlToFileUsingCommonsIO(url, epgFile);
				// Utils.copyUrlToFileUsingNIO(url, epgFile);
			}

			log.info("Reading epg.xml");
			doc = xm.readValue(new File(epgFile), XmltvDoc.class);
		} catch (JsonParseException jpe) {
			log.info("Error parsing {} , invalid xml format", epgFile);
		} catch (IOException ioe) {
			log.info("Error reading {} - {}", epgFile, ioe.getMessage());
		} catch (Exception e) {
			log.info("Error reading {} - {}", epgFile, e.getMessage());
		}

		return doc;
	}

	private static void writeJson(List<XmltvChannel> channels, XmltvDoc doc) {

		JsonObject jsonEPG = new JsonObject();
		StringBuffer jsonString = new StringBuffer();
		JsonArray jsonChannels = new JsonArray();

		for (XmltvChannel channel : channels) {

			JsonObject thisChannel = new JsonObject();
			thisChannel.addProperty("display_name", channel.getDisplayNames().get(0).getText());

			JsonArray programmes = new JsonArray();

			List<XmltvProgramme> foundByStream = doc.getProgrammesById(channel.getId());

			if (foundByStream != null && foundByStream.isEmpty()) {
				for (XmltvProgramme programme : foundByStream) {
					JsonObject thisProgramme = new JsonObject();
					thisProgramme.addProperty("start", formatTime(programme.getStart()));
					thisProgramme.addProperty("stop", formatTime(programme.getStop()));
					thisProgramme.addProperty("title", programme.getTitle().getText());

					programmes.add(thisProgramme);
				}

				thisChannel.add("programmes", programmes);
				jsonChannels.add(thisChannel);
			}

			// jsonEPG.add("EPG", jsonChannels);

			Gson gson = new GsonBuilder().setPrettyPrinting().create();
			JsonElement jsonElement = JsonParser.parseString(jsonChannels.toString());
			String prettyJson = gson.toJson(jsonElement);

			log.info(prettyJson);
			try (FileWriter fileWriter = new FileWriter(new File("./epg.json"))){

				fileWriter.write(prettyJson);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}

	}

	private static String formatTime(String datetimeString) {

		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss X");
		ZonedDateTime zonedDateTime = ZonedDateTime.parse(datetimeString, formatter);

		LocalTime time = zonedDateTime.toLocalTime();
		return time.format(DateTimeFormatter.ofPattern("HH:mm")).toString();


	}

	public List<Channel> getEPGJson() {

		String epgJson = null;
		List<Channel> channels = null;

		try (BufferedInputStream bis = new BufferedInputStream(new File("./epg.json").toURI().toURL().openStream())){

			epgJson = IOUtils.toString(bis, "UTF-8"); 

			Gson gson = new Gson();

			Type listType = new TypeToken<List<Channel>>() {}.getType();
			channels = gson.fromJson(epgJson, listType);

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return channels;
	}
}
