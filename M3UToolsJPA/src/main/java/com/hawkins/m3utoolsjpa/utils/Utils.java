
package com.hawkins.m3utoolsjpa.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.file.Files;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.RegExUtils;
import org.apache.commons.lang3.StringUtils;

import com.hawkins.m3utoolsjpa.data.M3UItem;
import com.hawkins.m3utoolsjpa.data.M3UItemRepository;
import com.hawkins.m3utoolsjpa.properties.ConfigProperty;
import com.hawkins.m3utoolsjpa.properties.DownloadProperties;
import com.hawkins.m3utoolsjpa.regex.Patterns;
import com.hawkins.m3utoolsjpa.regex.RegexUtils;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class Utils {

    private static DownloadProperties dp = DownloadProperties.getInstance();

    public static File getPropertyFile(String propertyType) {
        File configFile = new File(FileUtilsForM3UToolsJPA.getCurrentWorkingDirectory() + propertyType);
        log.info("Reading properties from {}", configFile.getAbsolutePath());

        if (!configFile.exists() && log.isDebugEnabled()) {
            log.debug(configFile.getAbsolutePath() + " does not exist");
        }

        return configFile;
    }

    public static Properties readProperties(String propertyType) {
        long start = System.currentTimeMillis();
        Properties props = new Properties();

        try (FileInputStream fis = new FileInputStream(getPropertyFile(propertyType))) {
            if (propertyType.equals(Constants.DMPROPERTIES)) {
                props.load(fis);
            } else {
                props.loadFromXML(fis);
            }
            props.putIfAbsent("fileName", getPropertyFile(propertyType).getPath());
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
        try (OutputStream output = new FileOutputStream(Utils.getPropertyFile(Constants.CONFIGPROPERTIES))) {
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
        return readProperties(Constants.CONFIGPROPERTIES);
    }

    public static void copyUrlToFile(String url, String fileName) {
        long start = System.currentTimeMillis();

        try (FileOutputStream fileOS = new FileOutputStream(fileName);
             ReadableByteChannel readChannel = Channels.newChannel((new URI(url).toURL()).openStream())) {
            fileOS.getChannel().transferFrom(readChannel, 0L, Long.MAX_VALUE);
        } catch (IOException | URISyntaxException e) {
            if (log.isDebugEnabled()) {
                log.debug(e.getMessage());
            }
        }

        long end = System.currentTimeMillis();
        if (log.isDebugEnabled()) {
            log.debug("copyUrlToFile executed in {} ms", (end - start));
        }
    }

    public static void copyUrlToFileUsingCommonsIO(String url, String fileName) {
        String epgFile = Constants.EPG_XML;

        try {
            FileUtilsForM3UToolsJPA.backupFile(epgFile);
            FileUtils.copyURLToFile(new URI(url).toURL(), new File(fileName), 180000, 600000);
        } catch (MalformedURLException | URISyntaxException e) {
            FileUtilsForM3UToolsJPA.restoreFile(epgFile);
            log.info("{} - {}", e.getClass().getSimpleName(), e.getMessage());
        } catch (IOException e) {
            FileUtilsForM3UToolsJPA.restoreFile(epgFile);
            log.info("IOException - {}", e.getMessage());
        } catch (Exception e) {
            FileUtilsForM3UToolsJPA.restoreFile(epgFile);
            log.info("Exception - {}", e.getMessage());
        }
    }

    public static void copyUrlToFileUsingNIO(String url, String fileName) throws IOException {
        try {
            URL nioUrl = new URI(url).toURL();
            log.info("Retrieving file from {}", nioUrl.toString());

            try (ReadableByteChannel byteChannel = Channels.newChannel(nioUrl.openStream());
                 FileOutputStream outputStream = new FileOutputStream(fileName)) {
                outputStream.getChannel().transferFrom(byteChannel, 0, Long.MAX_VALUE);
            }
        } catch (MalformedURLException | URISyntaxException e) {
            e.printStackTrace();
        }
    }

    public static URL getFinalLocation(String address) throws IOException {
        long start = System.currentTimeMillis();
        String originalURL = address;

        try {
            URL url = new URI(address).toURL();
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            int status = conn.getResponseCode();
            if (status != HttpURLConnection.HTTP_OK) {
                if (status == HttpURLConnection.HTTP_MOVED_TEMP || status == HttpURLConnection.HTTP_MOVED_PERM
                        || status == HttpURLConnection.HTTP_SEE_OTHER) {
                    String newLocation = conn.getHeaderField("Location");
                    return getFinalLocation(newLocation);
                }
            }

                log.debug("Final URL is different to Original URL");

            if (log.isDebugEnabled()) {
                log.debug("getFinalLocation took {} ms", (System.currentTimeMillis() - start));
            }

            return new URI(address).toURL();
        } catch (MalformedURLException | URISyntaxException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static String format(double bytes, int digits) {
        String[] dictionary = {"bytes", "KB", "MB", "GB", "TB", "PB", "EB", "ZB", "YB"};
        int index = 0;
        for (index = 0; index < dictionary.length; index++) {
            if (bytes < 1024) {
                break;
            }
            bytes = bytes / 1024;
        }
        return ("%." + digits + "f").formatted(bytes) + " " + dictionary[index];
    }

    public static String printNow() {
        SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
        Date date = new Date();
        return formatter.format(date);
    }

    public static String replaceForwardSlashWithSpace(String stringToReplace) {
        if (stringToReplace != null && stringToReplace.contains("/")) {
            stringToReplace = stringToReplace.replace("/", " ");
        }
        return stringToReplace;
    }

    public static String normaliseName(String filmName) {
        int startIndex = 0;
        int endIndex = StringUtils.indexOfAny(filmName, new String[]{"SD", "FHD", "UHD", "HD", "4K"});

        if (endIndex != -1) {
            filmName = filmName.substring(startIndex, endIndex);
        }

        startIndex = filmName.indexOf(':');
        if (startIndex != -1) {
            filmName = filmName.replace(":", " ");
        }

        if (filmName.contains("(MULTISUB)")) {
            return filmName.replace("(MULTISUB)", "").trim();
        } else {
            return filmName.trim();
        }
    }

    public static String normaliseSearch(String filmName) {
        int startIndex = 0;
        int endIndex = StringUtils.indexOfAny(filmName, new String[]{"SD", "FHD", "UHD", "HD", "4K"});

        if (endIndex != -1) {
            filmName = filmName.substring(startIndex, endIndex - 1); // Exclude left bracket (
        }

        if (filmName.contains("(MULTISUB)")) {
            filmName = filmName.replace("(MULTISUB)", "").trim();
        }

        filmName = Utils.removeFromString(filmName, Patterns.STRIP_COUNTRY_IDENTIFIER);
        filmName = Utils.removeFromString(filmName, Patterns.BRACKETS_AND_CONTENT);
        filmName = RegexUtils.removeCountryIdentifier(filmName, dp.getIncludedCountries());
        return filmName.trim();
    }

    public static String removeFromString(String originalString, Pattern pattern) {
        return RegExUtils.removeAll(originalString, pattern);
    }

    public static String getParamsString(Map<String, String> params) throws UnsupportedEncodingException {
        StringBuilder result = new StringBuilder();

        for (Map.Entry<String, String> entry : params.entrySet()) {
            result.append(URLEncoder.encode(entry.getKey(), StandardCharsets.UTF_8));
            result.append("=");
            result.append(URLEncoder.encode(entry.getValue(), StandardCharsets.UTF_8));
            result.append("&");
        }

        String resultString = result.toString();
        return resultString.length() > 0 ? resultString.substring(0, resultString.length() - 1) : resultString;
    }

    public static String deriveGroupTypeByUrl(String url) {
        if (url.contains(Constants.SERIES)) {
            return Constants.SERIES;
        } else if (url.contains(Constants.MOVIE)) {
            return Constants.MOVIE;
        } else if (url.length() > 0) {
            return Constants.LIVE;
        }
        return "";
    }

    public static String getFileExtension(String url) {
        return url.substring(url.length() - 3);
    }

    public static String getURLFromName(String filmName, M3UItemRepository m3uItemRepository) {
        List<M3UItem> filmList = m3uItemRepository.findByTvgName(filmName);
        for (M3UItem m3uItem : filmList) {
            if (m3uItem.getTvgName().equalsIgnoreCase(filmName)) {
                return m3uItem.getChannelUri();
            }
        }
        return null;
    }

    public static boolean fileOlderThan(File fileName, int ageInMinutes) throws IOException {
        BasicFileAttributes attr = Files.readAttributes(fileName.toPath(), BasicFileAttributes.class);
        FileTime fileTime = attr.creationTime();
        long diff = System.currentTimeMillis() - fileTime.toMillis();
        long minutes = TimeUnit.MILLISECONDS.toMinutes(diff);
        log.info("{} is {} minutes old", fileName.toString(), String.valueOf(minutes));
        return minutes > ageInMinutes;
    }

    public static int indexOfAny(String search, String[] items) {
        return StringUtils.indexOfAny(search, items);
    }

    public static void writeToFile(File thisFile, String content) throws IOException {
        if (log.isDebugEnabled()) {
            log.debug("Writing file {}", thisFile.getAbsolutePath());
        }
        try (FileWriter writer = new FileWriter(thisFile)) {
            writer.write(content);
        }
    }
}
