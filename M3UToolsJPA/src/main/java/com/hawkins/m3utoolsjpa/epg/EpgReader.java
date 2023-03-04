package com.hawkins.m3utoolsjpa.epg;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeParseException;
import java.util.Iterator;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.SAXReader;
import org.dom4j.io.XMLWriter;
import org.springframework.util.StopWatch;

import com.hawkins.dmanager.util.Utils;
import com.hawkins.m3utoolsjpa.emby.EmbyApi;
import com.hawkins.m3utoolsjpa.properties.DownloadProperties;
import com.hawkins.m3utoolsjpa.utils.Constants;

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
	
	public static String changeLocalTime(String timeString) {
		
		String epgTimeDifference = DownloadProperties.getInstance().getEpgTimeDifference();
		
		// ZonedDateTime defaultZonedDateTime = ZonedDateTime.of(2020, 01, 01, 0, 0, 0, 0, ZoneId.of("UTC"));
		

		/*
		 * try { ZonedDateTime zonedDateTime = ZonedDateTime.parse(timeString); }
		 * catch(DateTimeParseException e) {
		 * log.info("DateTimeParserEception for String {}", timeString); timeString =
		 * defaultZonedDateTime.toString(); }
		 */
		
		String updatedTimeString = timeString.replace("+0000", epgTimeDifference);
		
		return updatedTimeString;
		
	}

	public static void createEPG() {
		
			
		String downloadedXML = Constants.DOWNLOADED_XML;
		
			SAXReader reader = new SAXReader();
			DownloadProperties dp = DownloadProperties.getInstance();
			
			StopWatch sw = new StopWatch();
			sw.start();
			File xmlFile = Utils.copyUrlToFile(dp.getStreamXMLUrl(), downloadedXML);
			sw.stop();
			
			log.info("Downloading {} took {}ms", dp.getStreamXMLUrl(), sw.getTotalTimeMillis());
			try {
				
				
				sw.start();
				document = reader.read(downloadedXML);
				sw.stop();
				
				log.info("Reading {} took {}ms", downloadedXML, sw.getTotalTimeMillis());
				
				Element rootElement = document.getRootElement();

				/*
				 * 
				 *  Now we need to make any adjustments to the programme start and end dates
				 */
				
				
				Iterator<Element> itProgramme = rootElement.elementIterator("programme");

				while (itProgramme.hasNext() ) {
					Element pgmElement = (Element) itProgramme.next();

					szStart = pgmElement.attribute(START).getStringValue();
					szEnd = pgmElement.attribute(STOP).getStringValue();
				
					String szNewStart = changeLocalTime(szStart);
					String szNewEnd = changeLocalTime(szEnd);
					
					pgmElement.attribute(START).setText(szNewStart);
					pgmElement.attribute(STOP).setText(szNewEnd);
				}
				
				OutputFormat format = OutputFormat.createPrettyPrint();
				XMLWriter writer;
				
				String epgFile = Constants.EPG_XML;
						
				log.info("Writing {}", epgFile);
				writer = new XMLWriter(new BufferedOutputStream(new FileOutputStream(epgFile)), format);
				
				writer.write(document);
				writer.close();
				
				if (xmlFile.exists()) xmlFile.delete();
				
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


}
