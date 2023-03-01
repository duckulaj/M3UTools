package com.hawkins.m3utoolsjpa.service;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.SAXReader;
import org.dom4j.io.XMLWriter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.hawkins.m3utoolsjpa.data.M3UItem;
import com.hawkins.m3utoolsjpa.data.M3UItemRepository;
import com.hawkins.m3utoolsjpa.data.SelectedChannel;
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
		
		log.info("Found {} m3uItems", m3uItems.size());
		log.info("Found {} channels", xmltvChannels.size());
		log.info("Found {} programmes", xmltvProgrammes.size());
		
		for (XmltvChannel xmltvChannel : xmltvChannels) {
			for (M3UItem m3uItem : m3uItems) {
			
				if (xmltvChannel.getDisplayNames().get(0).getText().contains(m3uItem.getChannelName())) {
					xmltvChannel.setId(m3uItem.getTvgId());
					selectedXmltvChannels.add(xmltvChannel);
					
				}
			}
		}
		
		log.info("Processing {} selected channels", selectedXmltvChannels.size());
		
		for (XmltvChannel channel : selectedXmltvChannels) {
			for (XmltvProgramme xmltvProgramme : xmltvProgrammes) {
				if (xmltvProgramme.getChannel().contains(channel.getId())) {
					// xmltvProgramme.setChannel(channel.getDisplayNames().get(0).getText());
					xmltvProgramme.setChannel(channel.getId());
					xmltvProgramme.setIcon(new XmltvIcon("", "", ""));
					xmltvProgramme.setCredits("");
					xmltvProgramme.setVideo(new XmltvVideo("HDTV"));
					// xmltvProgramme.setStart(ZonedDateTime.parse(EpgReader.changeLocalTime(xmltvProgramme.getStart().toString())));
					// xmltvProgramme.setStop(ZonedDateTime.parse(EpgReader.changeLocalTime(xmltvProgramme.getStop().toString())));
					// log.info("Programme Date is {}", DateTimeFormatter.ofLocalizedDate(FormatStyle.FULL).format(xmltvProgramme.getStart()));
					selectedXmltvProgrammes.add(xmltvProgramme);
				}
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
		}
	}

public void readEPGUsingSax() {
		
		DownloadProperties dp = DownloadProperties.getInstance();
		
		log.info("Passing control to createEPG");
		EpgReader.createEPG();
		
		
		try {
			SAXReader reader = new SAXReader();
			Document document = reader.read("./epg.xml");
			Element rootElement = document.getRootElement();
			String generatorName = rootElement.attribute("generator-info-name").getStringValue();
			
			Iterator<Element> itChannel = rootElement.elementIterator("channel");
			Iterator<Element> itProgramme = rootElement.elementIterator("programme");
			
			List<M3UItem> m3uItems = itemRepository.findTvChannelsBySelected(true);
			
			List<Element> selectedChannels = new ArrayList<Element>();
						
			Document selectedDocument = DocumentHelper.createDocument();
			Element root = selectedDocument.addElement("tv");
			root.addAttribute("generator-info-name", generatorName);
			
			for (M3UItem m3uItem : m3uItems) {
			
				while (itChannel.hasNext() ) {
					Element chElement = (Element) itChannel.next();
					
				
					if (chElement.attribute("id").getStringValue().equalsIgnoreCase(m3uItem.getTvgId())) {
						selectedChannels.add(chElement);
						root.appendAttributes(chElement);
						
					}
				}
			}
			
			while (itProgramme.hasNext() ) {
				Element pgmElement = (Element) itProgramme.next();
				for (Element selectedChannel : selectedChannels) {
				
					if (selectedChannel.attribute("id").getStringValue().equalsIgnoreCase(pgmElement.attribute("channel").getStringValue())) {
						root.appendAttributes(pgmElement);
						
					}
				}
			}
			
			
			
			OutputFormat format = OutputFormat.createPrettyPrint();
			XMLWriter writer;
			
			String epgFile = "./generatedChannels.xml";
					
			log.info("Writing {}", epgFile);
			writer = new XMLWriter(new BufferedOutputStream(new FileOutputStream(epgFile)), format);
			
			writer.write(selectedDocument);
			writer.close();
			
			log.info("Written ./generatedChannels.xml");
			
			if (dp.isEmbyInstalled()) {
				EmbyApi.refreshGuide();
			}
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
