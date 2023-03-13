package com.hawkins.m3utoolsjpa.service;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.collections4.MultiValuedMap;
import org.apache.commons.collections4.multimap.ArrayListValuedHashMap;
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

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class EpgService {

	@Autowired
	M3UItemRepository itemRepository;

	public void readEPG() {

		DownloadProperties dp = DownloadProperties.getInstance();

		log.info("Passing control to createEPG");
		EpgReader.createEPG();

		XmltvDoc selectedXmltvDoc = new XmltvDoc();
		List<XmltvChannel> selectedXmltvChannels = new ArrayList<XmltvChannel>();
		List<XmltvProgramme> selectedXmltvProgrammes = new ArrayList<XmltvProgramme>();

		XmlMapper xm = XmltvUtils.createMapper();
		// XmlMapper xm = new XmlMapper();
		XmltvDoc doc = new XmltvDoc();

		try {
			log.info("Reading epg.xml");
			doc = xm.readValue(new File("./epg.xml"), XmltvDoc.class);
			// xm.writeValue(new File("/home/jonathan/.xteve/data/ReWrittenChannels.xml"), doc);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		// Now that we have the XmltvDoc we can extract the channels that we have selected

		List<M3UItem> m3uItems = itemRepository.findTvChannelsBySelected(true);
		List<XmltvChannel> xmltvChannels = doc.getChannels();
		List<XmltvProgramme> xmltvProgrammes = doc.getProgrammes();




		for (XmltvChannel xmltvChannel : xmltvChannels) { for (M3UItem m3uItem :
			m3uItems) {

			if (xmltvChannel.getDisplayNames().get(0).getText().contains(m3uItem.
					getChannelName())) { xmltvChannel.setId(m3uItem.getTvgId());
					selectedXmltvChannels.add(xmltvChannel);

			} } }

		log.info("Processing {} selected channels", selectedXmltvChannels.size());

		for (XmltvChannel channel : selectedXmltvChannels) { 
			for (XmltvProgramme	xmltvProgramme : xmltvProgrammes) { 
				if (xmltvProgramme.getChannel().contains(channel.getId())) {
					xmltvProgramme.setChannel(channel.getDisplayNames().get(0).getText());
					xmltvProgramme.setChannel(channel.getId()); 
					xmltvProgramme.setIcon(new XmltvIcon("", "", "")); 
					xmltvProgramme.setCredits("");
					xmltvProgramme.setVideo(new XmltvVideo("HDTV"));
					// xmltvProgramme.setStart(ZonedDateTime.parse(EpgReader.changeLocalTime(xmltvProgramme.getStart().toString())));
					//xmltvProgramme.setStop(ZonedDateTime.parse(EpgReader.changeLocalTime(xmltvProgramme.getStop().toString()))); 
					// log.info("Programme Date is {}", DateTimeFormatter.ofLocalizedDate(FormatStyle.FULL).format(xmltvProgramme.getStart()));
					selectedXmltvProgrammes.add(xmltvProgramme); 
				} 
			} 
		}


		MultiValuedMap<String, XmltvProgramme> programmesMap = programmeListToMap(xmltvProgrammes);
		MultiValuedMap<String,XmltvChannel> channelsMap = channelListToMap(xmltvChannels);

		log.info("Found {} m3uItems", m3uItems.size());
		log.info("Found {} channels", xmltvChannels.size());

		if (xmltvProgrammes != null) {
			log.info("Found {} programmes", xmltvProgrammes.size());
		} else {
			log.info("No programmes found");
		}


		for (M3UItem m3uItem : m3uItems) {
			selectedXmltvChannels.addAll(channelsMap.get(m3uItem.getTvgId())); 
		}

		log.info("Processing {} selected channels", selectedXmltvChannels.size());

		for (XmltvChannel channel : selectedXmltvChannels) {
			selectedXmltvProgrammes.addAll(programmesMap.get(channel.getId())); 
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
		}
	}

	private MultiValuedMap<String, XmltvChannel> channelListToMap(List<XmltvChannel> xmltvChannels) {

		MultiValuedMap<String, XmltvChannel> xmltvChannelsMap = new ArrayListValuedHashMap<>();

		for (XmltvChannel channel : xmltvChannels) {

			String channelName = channel.getDisplayNames().get(0).getText().replaceAll("[^\\x00-\\x7F]", "");
			xmltvChannelsMap.put(channelName.trim(), channel);
		}

		return xmltvChannelsMap;
	}

	private MultiValuedMap<String, XmltvProgramme> programmeListToMap(List<XmltvProgramme> xmltvProgrammes) {

		MultiValuedMap<String, XmltvProgramme> xmltvProgrammesMap = new ArrayListValuedHashMap<>();

		for (XmltvProgramme programme : xmltvProgrammes) {

			String channelName = programme.getChannel().replaceAll("[^\\x00-\\x7F]", "");
			xmltvProgrammesMap.put(channelName, programme);
		}

		return xmltvProgrammesMap;
	}
}
