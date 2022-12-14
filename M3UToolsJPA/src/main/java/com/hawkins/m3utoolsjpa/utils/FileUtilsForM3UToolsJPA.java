package com.hawkins.m3utoolsjpa.utils;



import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

import org.apache.commons.io.input.ReversedLinesFileReader;

import com.hawkins.dmanager.DownloadEntry;

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
	
	public static void copyToriginalFileName (DownloadEntry d) {

		try {
			Path copied = Paths.get(d.getFolder() + d.getFile());
			Path originalPath = Paths.get(d.getFolder() + d.getOriginalFileName());
			Files.copy(copied, originalPath, StandardCopyOption.REPLACE_EXISTING);

			Files.isSameFile(copied, originalPath);

			Files.deleteIfExists(copied);

		} catch (IOException ioe) {
			if (log.isDebugEnabled()) {
				log.debug(ioe.getMessage());
			}
		}


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
	
}
