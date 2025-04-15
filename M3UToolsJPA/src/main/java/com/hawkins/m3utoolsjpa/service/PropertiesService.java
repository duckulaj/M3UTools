package com.hawkins.m3utoolsjpa.service;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;

import org.apache.commons.collections4.properties.SortedProperties;
import org.springframework.stereotype.Service;

import com.hawkins.m3utoolsjpa.properties.ConfigProperty;
import com.hawkins.m3utoolsjpa.properties.DownloadProperties;
import com.hawkins.m3utoolsjpa.properties.OrderedProperties;
import com.hawkins.m3utoolsjpa.utils.Constants;
import com.hawkins.m3utoolsjpa.utils.FileUtilsForM3UToolsJPA;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class PropertiesService {

	private static DownloadProperties dp = DownloadProperties.getInstance();

    public static File getPropertyFile() {
        File configFile = new File(FileUtilsForM3UToolsJPA.getCurrentWorkingDirectory() + Constants.CONFIGPROPERTIES);
        log.info("Reading properties from {}", configFile.getAbsolutePath());

        if (!configFile.exists() && log.isDebugEnabled()) {
            log.debug(configFile.getAbsolutePath() + " does not exist");
        }

        return configFile;
    }

    public static Properties readProperties() {
        long start = System.currentTimeMillis();
        Properties props = new Properties();

        try (FileInputStream fis = new FileInputStream(getPropertyFile())) {
            props.loadFromXML(fis);
            props.putIfAbsent("fileName", new File(Constants.CONFIGPROPERTIES).getPath());
        } catch (FileNotFoundException fnfe) {
            log.debug(fnfe.toString());
        } catch (IOException ioe) {
            log.debug(ioe.toString());
        }

        long end = System.currentTimeMillis();
        if (log.isDebugEnabled()) {
            log.debug("readProperties executed in {} ms", (end - start));
        }
        return props;
    }

    public static Properties saveProperties(ConfigProperty configProperty) {
        try (OutputStream output = new FileOutputStream(getPropertyFile())) {
            Properties props = DownloadProperties.getInstance().getProps();
            props.forEach((key, value) -> {
                if (key.equals(configProperty.getKey())) {
                    props.replace(configProperty.getKey(), configProperty.getValue());
                }
            });
            props.storeToXML(output, "");
        } catch (IOException io) {
            if (log.isDebugEnabled()) {
                log.debug(io.getMessage());
            }
        }
        return readProperties();
    }

    public static OrderedProperties getOrderProperties() {

		SortedProperties sortedProperties = new SortedProperties();
		OrderedProperties properties = new OrderedProperties();
		File propertyFile = getPropertyFile();
		try {
			properties.load(new FileReader(propertyFile));
			properties = OrderedProperties.copyOf(properties);
			sortedProperties.load(new FileReader(propertyFile));
		} catch (IOException ioe) {
			log.info("IOException occurred - {}", ioe.getMessage());
		}

		return properties;

	}

	public static Set<Entry<String, String>> getOrderedPropertiesEntrySet() {

		OrderedProperties properties = new OrderedProperties();
		try {
			properties.loadFromXML(new FileInputStream(new File(dp.getPropertiesFile())));
			properties = OrderedProperties.copyOf(properties);
		} catch (IOException ioe) {
			log.info("IOException occurred - {}", ioe.getMessage());
		}

		return properties.entrySet();
	}

	public static void updateProperty(ConfigProperty configProperty) {

		log.info(configProperty.toString());
		DownloadProperties.getInstance().updateProperty(configProperty);
	}

}
