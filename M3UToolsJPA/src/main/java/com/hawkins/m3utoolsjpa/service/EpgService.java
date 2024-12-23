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
import java.util.Objects;
import java.util.stream.Collectors;

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

		XmltvDoc doc = getXmlTvDoc();
		List<M3UItem> m3uItems = itemRepository.findTvChannelsBySelected(true);

		List<XmltvChannel> selectedXmltvChannels = getSelectedChannels(doc, m3uItems);
		List<XmltvProgramme> selectedXmltvProgrammes = getSelectedProgrammes(doc, selectedXmltvChannels);

		log.info("Found {} programmes", selectedXmltvProgrammes.size());

		XmltvDoc selectedXmltvDoc = new XmltvDoc();
		selectedXmltvDoc.setGeneratorName(doc.getGeneratorName());
		selectedXmltvDoc.setSourceInfoName(doc.getSourceInfoName());
		selectedXmltvDoc.setChannels(selectedXmltvChannels);
		selectedXmltvDoc.setProgrammes(selectedXmltvProgrammes);

		writeXmltvDocToFile(selectedXmltvDoc, "./generatedChannels.xml");

		if (dp.isEmbyInstalled()) {
			EmbyApi.refreshGuide();
		}

		log.info("readEPG finished at {}", Utils.printNow());
	}


private List<XmltvChannel> getSelectedChannels(XmltvDoc doc, List<M3UItem> m3uItems) {
    List<XmltvChannel> selectedXmltvChannels = m3uItems.stream()
        .map(m3uItem -> doc.getChannelsByIdAndName(m3uItem.getTvgId(), m3uItem.getTvgName()))
        .filter(Objects::nonNull)
        .collect(Collectors.toList());

    log.info("Processing {} selected channels", selectedXmltvChannels.size());
    return selectedXmltvChannels;
}



private List<XmltvProgramme> getSelectedProgrammes(XmltvDoc doc, List<XmltvChannel> selectedXmltvChannels) {
    return selectedXmltvChannels.stream()
        .map(channel -> doc.getProgrammesById(channel.getId()))
        .filter(Objects::nonNull)
        .flatMap(List::stream)
        .peek(xmltvProgramme -> {
            xmltvProgramme.setChannel(xmltvProgramme.getChannel());
            xmltvProgramme.setIcon(new XmltvIcon("", "", ""));
            xmltvProgramme.setCredits("");
            xmltvProgramme.setVideo(new XmltvVideo("HDTV"));
            xmltvProgramme.setStart(EpgReader.changeLocalTime(xmltvProgramme.getStart().toString()));
            xmltvProgramme.setStop(EpgReader.changeLocalTime(xmltvProgramme.getStop().toString()));
        })
        .collect(Collectors.toList());
}


	private void writeXmltvDocToFile(XmltvDoc doc, String filePath) {
		try {
			XmlMapper xm = XmltvUtils.createMapper();
			xm.writerWithDefaultPrettyPrinter().writeValue(new File(filePath), doc);
			log.info("Written {}", filePath);
		} catch (IOException e) {
			log.error("Error writing to file {}: {}", filePath, e.getMessage());
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
