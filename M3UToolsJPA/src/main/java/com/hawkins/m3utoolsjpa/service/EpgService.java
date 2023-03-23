package com.hawkins.m3utoolsjpa.service;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.commons.collections4.MultiValuedMap;
import org.apache.commons.collections4.multimap.ArrayListValuedHashMap;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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

	public void readEPG() {
		
		log.info("readEPG started at {}", Utils.printNow());

		DownloadProperties dp = DownloadProperties.getInstance();

		// log.info("Passing control to createEPG");
		// EpgReader.createEPG();
		
		String epgFile = Constants.EPG_XML;

		XmltvDoc selectedXmltvDoc = new XmltvDoc();
		List<XmltvChannel> selectedXmltvChannels = new ArrayList<XmltvChannel>();
		List<XmltvProgramme> selectedXmltvProgrammes = new ArrayList<XmltvProgramme>();

		XmlMapper xm = XmltvUtils.createMapper();
		// XmlMapper xm = new XmlMapper();
		XmltvDoc doc = new XmltvDoc();

		try {
			log.info("Retrieving EPG from remote source");
			Utils.copyUrlToFile(dp.getStreamXMLUrl(), epgFile);
			log.info("Reading epg.xml");
			doc = xm.readValue(new File(epgFile), XmltvDoc.class);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		// Now that we have the XmltvDoc we can extract the channels that we have selected

		List<M3UItem> m3uItems = itemRepository.findTvChannelsBySelected(true);
		List<XmltvChannel> xmltvChannels = doc.getChannels();
		List<XmltvProgramme> xmltvProgrammes = doc.getProgrammes();

		log.info("Found {} selected m3uItems", m3uItems.size());
		log.info("Found {} channels", xmltvChannels.size());

		if (xmltvProgrammes != null) {
			log.info("Found {} programmes", xmltvProgrammes.size());
		} else {
			log.info("No programmes found");
		}

		
		 // We need to ensure that the tvg-id in the epg matches the tvg-id for the m3u Item
		  
		 for (XmltvChannel xmltvChannel : xmltvChannels) { 
			 for (M3UItem m3uItem : m3uItems) {
				 if (xmltvChannel.getDisplayNames().get(0).getText().contains(m3uItem.getChannelName())) { 
					 XmltvChannel channel = new XmltvChannel();
					 channel.setId(String.valueOf(m3uItem.getTvgId()));
					 channel.setIcon(new XmltvIcon(m3uItem.getTvgLogo()));
					 channel.setDisplayNames(xmltvChannel.getDisplayNames());
					 
					 selectedXmltvChannels.add(channel);
		 		 }
			 } 
		 }
		  
		  log.info("Processing {} selected channels", selectedXmltvChannels.size());
		  
		  if (xmltvProgrammes != null) { 
			  for (XmltvChannel channel : selectedXmltvChannels) { 
				  for (XmltvProgramme xmltvProgramme : xmltvProgrammes) { 
					  if (xmltvProgramme.getChannel().contains(channel.getId())) { 
						  // xmltvProgramme.setChannel(channel.getDisplayNames().get(0).getText());
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
		 
		
		MultiValuedMap<String, XmltvProgramme> programmesMap = programmeListToMap(xmltvProgrammes);
		MultiValuedMap<String,XmltvChannel> channelsMap = channelListToMap(selectedXmltvChannels);

		


		/*
		 * for (M3UItem m3uItem : m3uItems) {
		 * selectedXmltvChannels.addAll(channelsMap.get(m3uItem.getChannelName().trim())
		 * ); }
		 */

		log.info("Processing {} selected channels", selectedXmltvChannels.size());

		for (XmltvChannel channel : selectedXmltvChannels) {
			if (StringUtils.isNotEmpty(channel.getId())) {
				
				log.info("{} selectedXmltvProgrammes before mapping match", selectedXmltvProgrammes.size());
				log.info("finding programmes for channel {}", channel.getId());
				Collection<XmltvProgramme> foundPrograms = programmesMap.get(channel.getId());
				selectedXmltvProgrammes.addAll(programmesMap.get(channel.getId())); 
				log.info("Found {} programmes for channel {}", programmesMap.get(channel.getId()).size(), channel.getId());
			}
		}

		log.info("Found {} programmes", selectedXmltvProgrammes.size());

		selectedXmltvDoc.setGeneratorName(doc.getGeneratorName());
		selectedXmltvDoc.setSourceInfoName(doc.getSourceInfoName());
		selectedXmltvDoc.setChannels(selectedXmltvChannels);
		selectedXmltvDoc.setProgrammes(selectedXmltvProgrammes);

		try {
			xm.writerWithDefaultPrettyPrinter();
			xm.writeValue(new File("./generatedChannels.xml"), selectedXmltvDoc);
			log.info("Written ./generatedChannels.xml");

			if (dp.isEmbyInstalled()) {
				EmbyApi.refreshGuide();
			}

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			log.info("readEPG finished at {}", Utils.printNow());
		}
	}

	private MultiValuedMap<String, XmltvChannel> channelListToMap(List<XmltvChannel> xmltvChannels) {

		MultiValuedMap<String, XmltvChannel> xmltvChannelsMap = new ArrayListValuedHashMap<>();

		if (xmltvChannels != null) {
			for (XmltvChannel channel : xmltvChannels) {
	
				xmltvChannelsMap.put(channel.getId(), channel);
			}

		}
		
		return xmltvChannelsMap;
	}

	private MultiValuedMap<String, XmltvProgramme> programmeListToMap(List<XmltvProgramme> xmltvProgrammes) {

		MultiValuedMap<String, XmltvProgramme> xmltvProgrammesMap = new ArrayListValuedHashMap<>();

		if (xmltvProgrammes != null) {
			for (XmltvProgramme programme : xmltvProgrammes) {
	
				xmltvProgrammesMap.put(programme.getChannel(), programme);
			}
		}
		
		return xmltvProgrammesMap;
	}
	
	
}
