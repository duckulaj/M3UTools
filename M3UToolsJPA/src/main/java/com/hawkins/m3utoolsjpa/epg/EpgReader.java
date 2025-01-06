
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
import org.springframework.util.StopWatch;

import com.hawkins.m3utoolsjpa.emby.EmbyApi;
import com.hawkins.m3utoolsjpa.properties.DownloadProperties;
import com.hawkins.m3utoolsjpa.utils.Constants;
import com.hawkins.m3utoolsjpa.utils.Utils;

import lombok.extern.slf4j.Slf4j;

/**
 * Utility class for reading and processing EPG data.
 */
@Slf4j
public class EpgReader {

    public static final DownloadProperties properties = DownloadProperties.getInstance();
    private static Document document;
    private static String szEnd = "";
    private static String szStart = "";

    private static final String START = "start";
    private static final String STOP = "stop";

    /**
     * Default constructor.
     */
    public EpgReader() {
        super();
    }

    /**
     * Changes the local time in the given time string.
     *
     * @param timeString the time string
     * @return the updated time string
     */
    public static String changeLocalTime(String timeString) {
        String epgTimeDifference = properties.getEpgTimeDifference();
        int position = timeString.lastIndexOf("+");

        if (position < 0) position = timeString.lastIndexOf("-");

        if (position < 0) return timeString;

        return timeString.substring(0, position) + epgTimeDifference;
    }

    /**
     * Creates the EPG by downloading and processing the XML data.
     */
    public static void createEPG() {
        String downloadedXML = Constants.DOWNLOADED_XML;
        SAXReader reader = new SAXReader();
        DownloadProperties dp = properties;

        StopWatch sw = new StopWatch();
        sw.start();
        Utils.copyUrlToFile(dp.getStreamXMLUrl(), downloadedXML);
        sw.stop();

        log.info("Downloading {} took {}ms", dp.getStreamXMLUrl(), sw.getTotalTimeMillis());

        try {
            sw.start();
            document = reader.read(downloadedXML);
            sw.stop();

            log.info("Reading {} took {}ms", downloadedXML, sw.getTotalTimeMillis());

            Element rootElement = document.getRootElement();
            Iterator<Element> itProgramme = rootElement.elementIterator("programme");

            while (itProgramme.hasNext()) {
                Element pgmElement = itProgramme.next();
                szStart = pgmElement.attribute(START).getStringValue();
                szEnd = pgmElement.attribute(STOP).getStringValue();

                String szNewStart = changeLocalTime(szStart);
                String szNewEnd = changeLocalTime(szEnd);

                pgmElement.attribute(START).setText(szNewStart);
                pgmElement.attribute(STOP).setText(szNewEnd);
            }

            OutputFormat format = OutputFormat.createPrettyPrint();
            String epgFile = Constants.EPG_XML;

            log.info("Writing {}", epgFile);
            XMLWriter writer = null;
            try {
                writer = new XMLWriter(new BufferedOutputStream(new FileOutputStream(epgFile)), format);
                writer.write(document);
            } finally {
                if (writer != null) {
                    writer.close();
                }
            }

            if (dp.isEmbyInstalled()) {
                EmbyApi.refreshGuide();
            }

        } catch (DocumentException | UnsupportedEncodingException e) {
            log.error("Error processing EPG data: {}", e.getMessage(), e);
        } catch (IOException e) {
            log.error("I/O error: {}", e.getMessage(), e);
        }
    }
}
