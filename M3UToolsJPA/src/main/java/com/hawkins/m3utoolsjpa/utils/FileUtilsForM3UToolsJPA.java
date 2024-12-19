package com.hawkins.m3utoolsjpa.utils;



import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
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

	public static String fileTail (String filename, int linecount) {

		StringBuffer sb = new StringBuffer();
		String lineSeperator = System.getProperty("line.separator");

		File file = new File(filename);
		int counter = 0; 

		if (!file.exists()) {
			return "Log file not found";
		}
		int linesInFile = numberOfLines(file);

		// Can't read more lines than exist in the file
		
		if (linecount > linesInFile) {
			linecount = linesInFile;
		}

		try {
			// ReversedLinesFileReader object = new ReversedLinesFileReader(file, Charset.forName("UTF-8"));
			
			ReversedLinesFileReader object = ReversedLinesFileReader.builder()
					   .setPath(file.getPath())
					   .setBufferSize(4096)
					   .setCharset(StandardCharsets.UTF_8)
					   .get();

			while(counter < linecount) {
				sb.append(object.readLine());
				sb.append(lineSeperator);
				counter++;
			}

			object.close();

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return sb.toString();

	}

	public static int numberOfLines (File thisFile) {

		int lineCount = 0;

		FileReader fileReader;
		
		try {
			fileReader = new FileReader(thisFile);
			
			BufferedReader br = new BufferedReader(fileReader);
			while((br.readLine())!=null)
			{
				lineCount++; 

			}
			fileReader.close();		

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException ioe) {
			ioe.printStackTrace();
		}

		return lineCount;
	}
	
	

	public static String getCurrentWorkingDirectory() {
        
		String userDirectory = Paths.get("")
                .toAbsolutePath()
                .toString();
        
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
	
	public static void xmlToJsonConverter() {
        File xmlFile = new File("./generatedChannels.xml");
        File jsonFile = new File("./epg.json");

        // Ensure XML file exists
        if (!xmlFile.exists()) {
            log.info("XML file not found: " + xmlFile.getAbsolutePath());
            return;
        }

        XmlMapper xmlMapper = new XmlMapper();
        ObjectMapper objectMapper = new ObjectMapper();
        String jsonString;

        try {
            // Read XML file and convert to JsonNode
            JsonNode jsonNode = xmlMapper.readTree(xmlFile);
            
            // Convert JsonNode to pretty-printed JSON string
            jsonString = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(jsonNode);

            // Write JSON string to output file
            writeToFile(jsonFile, jsonString);
            
            log.info("Successfully converted XML to JSON and saved to: " + jsonFile.getAbsolutePath());
        } catch (IOException e) {
            log.info("Error converting XML to JSON", e);
        }
    }

    private static void writeToFile(File file, String content) {
        try {
            // Create directories if they don't exist
            Files.createDirectories(Paths.get(file.getParent()));

            // Write content to the file
            Files.write(file.toPath(), content.getBytes());
        } catch (IOException e) {
            log.info("Error writing to file: " + file.getAbsolutePath(), e);
        }
    }

    public static void main(String[] args) {
        xmlToJsonConverter();
    }	
	
}
