package com.hawkins.m3utoolsjpa.service;

import java.io.File;
import java.io.IOException;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.hawkins.dmanager.util.Utils;
import com.hawkins.m3utoolsjpa.data.M3UItem;
import com.hawkins.m3utoolsjpa.data.M3UItemRepository;
import com.hawkins.m3utoolsjpa.epg.XmltvChannel;
import com.hawkins.m3utoolsjpa.epg.XmltvDoc;
import com.hawkins.m3utoolsjpa.epg.XmltvIcon;
import com.hawkins.m3utoolsjpa.epg.XmltvProgramme;
import com.hawkins.m3utoolsjpa.epg.XmltvUtils;
import com.hawkins.m3utoolsjpa.epg.XmltvVideo;
import com.hawkins.m3utoolsjpa.properties.DownloadProperties;
import com.hawkins.m3utoolsjpa.utils.Constants;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class EpgService {

	@Autowired
	M3UItemRepository itemRepository;
	
	public void readEPG() {
		
		DownloadProperties dp = DownloadProperties.getInstance();
		
		XmltvDoc selectedXmltvDoc = new XmltvDoc();
		List<XmltvChannel> selectedXmltvChannels = new ArrayList<XmltvChannel>();
		List<XmltvProgramme> selectedXmltvProgrammes = new ArrayList<XmltvProgramme>();
		
		XmlMapper xm = XmltvUtils.createMapper();
		XmltvDoc doc = new XmltvDoc();
		
		try {
			File xmlFile = Utils.downloadFile(dp.getProps().getProperty(Constants.STREAM_PLAYLIST), "/home/jonathan/.xteve/data/interim.xml");
			doc = xm.readValue(xmlFile, XmltvDoc.class);
			// xm.writeValue(new File("/home/jonathan/.xteve/data/ReWrittenChannels.xml"), doc);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		// Now that we have the XmltvDoc we can extract the channels that we have selected
		
		List<M3UItem> m3uItems = itemRepository.findTvChannelsBySelected(true);
		List<XmltvChannel> xmltvChannels = doc.getChannels();
		List<XmltvProgramme> xmltvProgrammes = doc.getProgrammes();
		
		log.info("Found {} programmes", xmltvProgrammes.size());
		
		Long counter = 0L;
		for (XmltvProgramme p : xmltvProgrammes) {
			log.debug("Programme Date is {}", DateTimeFormatter.ofLocalizedDate(FormatStyle.FULL).format(p.getStart()));
			if (p.getStart().isAfter(ZonedDateTime.now())) {
				counter++;
			}
		}
		
		log.info("Counter = {}", counter);
		
		
		for (M3UItem m3uItem : m3uItems) {
			for (XmltvChannel xmltvChannel : xmltvChannels) {
				if (xmltvChannel.getDisplayNames().get(0).getText().contains(m3uItem.getChannelName())) {
					xmltvChannel.setId(m3uItem.getTvgChNo());
					selectedXmltvChannels.add(xmltvChannel);
					
				}
			}
		}
		
		for (XmltvChannel channel : xmltvChannels) {
			for (XmltvProgramme xmltvProgramme : xmltvProgrammes) {
				if (xmltvProgramme.getChannel().contains(channel.getId())) {
					xmltvProgramme.setChannel(channel.getDisplayNames().get(0).getText());
					xmltvProgramme.setIcon(new XmltvIcon("", "", ""));
					xmltvProgramme.setCredits("");
					xmltvProgramme.setVideo(new XmltvVideo("HDTV"));
					// log.info("Programme Date is {}", DateTimeFormatter.ofLocalizedDate(FormatStyle.FULL).format(xmltvProgramme.getStart()));
					selectedXmltvProgrammes.add(xmltvProgramme);
				}
			}
		}
		
		selectedXmltvDoc.setGeneratorName("M3UToolsJPA");
		selectedXmltvDoc.setSourceInfoName("M3UToolsJPA - 0.0.1");
		selectedXmltvDoc.setChannels(selectedXmltvChannels);
		selectedXmltvDoc.setProgrammes(selectedXmltvProgrammes);
		
		try {
			xm.writerWithDefaultPrettyPrinter();
			xm.writeValue(new File("/home/jonathan/.xteve/data/generatedChannels.xml"), selectedXmltvDoc);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
}
