package com.hawkins.m3utoolsjpa.epg;

import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Iterator;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.SAXReader;
import org.dom4j.io.XMLWriter;

import com.hawkins.m3utoolsjpa.emby.EmbyApi;
import com.hawkins.m3utoolsjpa.properties.DownloadProperties;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class EpgReader {
	
	public static DownloadProperties properties = DownloadProperties.getInstance();
	private static Document document;
	private static String szEnd = "";
	private static String szStart = "";
		
	private static final String START = "start";
	private static final String STOP = "stop";
		
	public EpgReader() {
		super();
	}
	
	public void changeLocalTime() {
		changeLocalTime(properties.getEpgFileName());
		
	}

	public static void changeLocalTime(String fileName) {
		
		

		// SimpleDateFormat xmlDateFormat = new SimpleDateFormat("yyyyMMddHHmmss Z");
		// SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

		SAXReader reader = new SAXReader();
		DownloadProperties dp = DownloadProperties.getInstance();
		
		String epgTimeDifference = dp.getEpgTimeDifference();
		try {
			document = reader.read(fileName);

			Element rootElement = document.getRootElement();

			/*
			 *  Now we need to make any adjustments to the programme start and end dates
			 */
			
			
			Iterator<Element> itProgramme = rootElement.elementIterator("programme");

			while (itProgramme.hasNext() ) {
				Element pgmElement = (Element) itProgramme.next();

				szStart = pgmElement.attribute(START).getStringValue();
				szEnd = pgmElement.attribute(STOP).getStringValue();
			
				String szNewStart = szStart.replace("+0000", epgTimeDifference);
				String szNewEnd = szEnd.replace("+0000", epgTimeDifference);
				
				pgmElement.attribute(START).setText(szNewStart);
				pgmElement.attribute(STOP).setText(szNewEnd);
			}
			
			OutputFormat format = OutputFormat.createPrettyPrint();
			XMLWriter writer;
			
			String outputFile = properties.getFileWatcherLocation() + "/xteveNew.xml";
			
			log.info("Writing {}", outputFile);
			writer = new XMLWriter(new BufferedOutputStream(new FileOutputStream(outputFile)), format);
			// writer = new XMLWriter(System.out, format);
			writer.write(document);
			writer.close();
			
			if (dp.isEmbyInstalled()) {
				EmbyApi.refreshGuide();
			}

		} catch (DocumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	public static String formatStr(String instr) {
		instr = instr.replaceAll("'","''");
		// instr = instr.replaceAll("[UK]'","");
        //instr = instr.replaceAll("[HD]","");
        // instr = instr.replaceAll("[\\[\\]\"]", "");
        /*instr = instr.replaceAll(">",">");
        instr = instr.replaceAll("<","<");*/
		return instr.trim();
	}
}
