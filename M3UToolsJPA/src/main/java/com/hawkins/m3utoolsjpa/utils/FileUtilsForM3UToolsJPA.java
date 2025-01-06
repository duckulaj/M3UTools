
package com.hawkins.m3utoolsjpa.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.input.ReversedLinesFileReader;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class FileUtilsForM3UToolsJPA {

    private FileUtilsForM3UToolsJPA() {
    }

    public static String fileTail(String filename, int linecount) {
        StringBuilder sb = new StringBuilder();
        String lineSeparator = System.lineSeparator();

        File file = new File(filename);
        if (!file.exists()) {
            return "Log file not found";
        }

        int linesInFile = numberOfLines(file);
        if (linecount > linesInFile) {
            linecount = linesInFile;
        }

        try (ReversedLinesFileReader object = ReversedLinesFileReader.builder()
                .setPath(file.getPath())
                .setBufferSize(4096)
                .setCharset(StandardCharsets.UTF_8)
                .get()) {

            for (int counter = 0; counter < linecount; counter++) {
                sb.append(object.readLine()).append(lineSeparator);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return sb.toString();
    }

    public static int numberOfLines(File thisFile) {
        try (BufferedReader br = new BufferedReader(new FileReader(thisFile))) {
            return (int) br.lines().count();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public static String getCurrentWorkingDirectory() {
        String userDirectory = Paths.get("").toAbsolutePath().toString();
        if (userDirectory.charAt(userDirectory.length() - 1) != File.separatorChar) {
            userDirectory += File.separator;
        }
        return userDirectory;
    }

    public static void backupFile(String fileName) {
        File file = new File(fileName);
        if (file.exists()) {
            File backupFile = new File(fileName + ".bu");
            try {
                FileUtils.copyFile(file, backupFile);
            } catch (IOException e) {
                log.debug("Error creating {}", backupFile.toString());
            }
        }
    }

    public static void restoreFile(String fileName) {
        File backupFile = new File(fileName + ".bu");
        File file = new File(fileName);
        if (backupFile.exists()) {
            try {
                FileUtils.copyFile(backupFile, file);
            } catch (IOException e) {
                log.debug("Error creating {}", file.toString());
            }
        }
    }

    public static void XmlToJsonConverter() {
        File xmlFile = new File("./generatedChannels.xml");
        XmlMapper xmlMapper = new XmlMapper();
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            JsonNode jsonNode = xmlMapper.readTree(xmlFile);
            String jsonString = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(jsonNode);
            Utils.writeToFile(new File("./epg.json"), jsonString);
        } catch (IOException e) {
            log.info(e.getMessage());
        }
    }
}
