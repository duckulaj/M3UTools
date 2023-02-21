package com.hawkins.m3utoolsjpa.service;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.hawkins.m3utoolsjpa.data.TvChannel;
import com.hawkins.m3utoolsjpa.data.TvChannelRepository;
import com.hawkins.m3utoolsjpa.epg.XmltvChannel;
import com.hawkins.m3utoolsjpa.epg.XmltvDoc;
import com.hawkins.m3utoolsjpa.epg.XmltvProgramme;
import com.hawkins.m3utoolsjpa.epg.XmltvUtils;

@Service
public class EpgService {

	@Autowired
	TvChannelRepository channelRepository;
	
	public XmltvDoc readEPG() {
		
		XmlMapper xm = XmltvUtils.createMapper();
		XmltvDoc doc = null;
		try {
			doc = xm.readValue(new File("/home/jonathan/.xteve/data/allChannels.xml"), XmltvDoc.class);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		// Now that we have the XmltvDoc we can extract the channels that we have selected
		
		Iterable<TvChannel> channels = channelRepository.findAll();
		List<XmltvChannel> xmltvChannels = doc.getChannels();
		List<XmltvProgramme> xmltvProgrammes = doc.getProgrammes();
		
		
		XmltvDoc selectedXmltvDoc = new XmltvDoc();
		List<XmltvChannel> selectedXmltvChannels = null;
		List<XmltvProgramme> selectedXmltvProgrammes = null;
		
		for (TvChannel channel : channels) {
			for (XmltvChannel xmltvChannel : xmltvChannels) {
				if (xmltvChannel.getDisplayNames().get(0).getText().equalsIgnoreCase(channel.getTvgName())) {
					
					xmltvChannel.setId(channel.getTvgChNo());
					selectedXmltvChannels.add(xmltvChannel);
					
				}
			}
			
			for (XmltvProgramme xmltvProgramme : xmltvProgrammes) {
				if (xmltvProgramme.getChannel().equals(channel.getTvgName())) {
					xmltvProgramme.setChannel(channel.getTvgChNo());
					selectedXmltvProgrammes.add(xmltvProgramme);
				}
			}
		}
		
		selectedXmltvDoc.setChannels(selectedXmltvChannels);
		selectedXmltvDoc.setProgrammes(selectedXmltvProgrammes);
		
		return selectedXmltvDoc;
	}
	
}
