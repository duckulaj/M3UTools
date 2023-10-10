package com.hawkins.m3utoolsjpa.service;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.hawkins.m3utoolsjpa.data.M3UItem;
import com.hawkins.m3utoolsjpa.data.M3UItemRepository;
import com.hawkins.m3utoolsjpa.emby.EmbyApi;
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
		}

		return doc;
	}

}
