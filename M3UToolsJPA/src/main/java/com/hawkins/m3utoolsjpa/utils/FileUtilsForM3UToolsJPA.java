package com.hawkins.m3utoolsjpa.utils;



import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Paths;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.input.ReversedLinesFileReader;

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
			ReversedLinesFileReader object = new ReversedLinesFileReader(file, Charset.forName("UTF-8"));

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
}
