
package com.hawkins.m3utoolsjpa.epg;

import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.introspect.VisibilityChecker;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import lombok.extern.slf4j.Slf4j;

/**
 * Utility class for handling XMLTV data.
 */
@Slf4j
public class XmltvUtils {

    public static final XmlMapper xmltvMapper = createMapper();
    private static final ByteArrayOutputStream bos = new ByteArrayOutputStream();

    /**
     * Creates and configures an XmlMapper for XMLTV data.
     *
     * @return the configured XmlMapper
     */
    public static XmlMapper createMapper() {
        return XmlMapper.builder()
                .configure(MapperFeature.AUTO_DETECT_GETTERS, false)
                .configure(MapperFeature.AUTO_DETECT_IS_GETTERS, false)
                .configure(MapperFeature.AUTO_DETECT_SETTERS, false)
                .configure(DeserializationFeature.ADJUST_DATES_TO_CONTEXT_TIME_ZONE, false)
                .defaultUseWrapper(false)
                .addModule(new JavaTimeModule())
                .visibility(new VisibilityChecker.Std(JsonAutoDetect.Visibility.NONE, JsonAutoDetect.Visibility.NONE, JsonAutoDetect.Visibility.NONE, JsonAutoDetect.Visibility.ANY, JsonAutoDetect.Visibility.ANY))
                .serializationInclusion(JsonInclude.Include.NON_NULL)
                .build();
    }

    /**
     * Parses XMLTV data from a byte array.
     *
     * @param data the XMLTV data as a byte array
     * @return the parsed XmltvDoc object, or null if an error occurs
     */
    public static XmltvDoc parseXmltv(byte[] data) {
        try (InputStream is = openStream(data)) {
            return xmltvMapper.readValue(is, XmltvDoc.class);
        } catch (IOException e) {
            log.error("Error parsing XMLTV data: {}", e.getMessage(), e);
            return null;
        }
    }

    /**
     * Opens an InputStream for the given byte array, handling GZIP compression if necessary.
     *
     * @param data the byte array
     * @return the InputStream
     * @throws IOException if an I/O error occurs
     */
    private static InputStream openStream(byte[] data) throws IOException {
        InputStream is = new ByteArrayInputStream(data);
        if (data.length >= 2 && data[0] == (byte) 0x1f && data[1] == (byte) 0x8b) {
            is = new GZIPInputStream(is);
        }
        return is;
    }

    /**
     * Serializes an XmltvDoc object to a byte array with GZIP compression.
     *
     * @param xmltv the XmltvDoc object
     * @return the serialized byte array
     */
    public static byte[] writeXmltv(XmltvDoc xmltv) {
        try {
            bos.reset();
            try (GZIPOutputStream gos = new GZIPOutputStream(bos);
                 BufferedOutputStream bbos = new BufferedOutputStream(gos)) {
                bbos.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?><!DOCTYPE tv SYSTEM \"xmltv.dtd\">\n".getBytes(StandardCharsets.UTF_8));
                xmltvMapper.writeValue(bbos, xmltv);
                bbos.flush();
            }
            return bos.toByteArray();
        } catch (IOException e) {
            log.error("Error serializing XMLTV data: {}", e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }
}
