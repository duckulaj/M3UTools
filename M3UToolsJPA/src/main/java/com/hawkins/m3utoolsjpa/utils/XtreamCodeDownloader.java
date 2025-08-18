package com.hawkins.m3utoolsjpa.utils;



import java.io.*;
import java.net.*;
import java.nio.channels.*;

public class XtreamCodeDownloader {

	// Replace these with your Xtream Codes API URL and login details
	private static final String API_URL = "http://cf.tvuhd.site";
	private static final String USERNAME = "6c501c0bea66";
	private static final String PASSWORD = "f72eb64ae0";

	public static void downloadM3UFile() {
		try {
			// Step 1: Get M3U URL from Xtream Codes API
			String m3uUrl = getM3UUrl();

			// Step 2: Download M3U file
			if (m3uUrl != null && !m3uUrl.isEmpty()) {
				downloadM3UFile(m3uUrl);
			} else {
				System.out.println("M3U URL not available.");
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	// Function to fetch the M3U URL from Xtream Codes API
	public static String getM3UUrl() throws Exception {
		// Construct API request
		String requestUrl = API_URL + "/get.php";  // Replace with the actual endpoint
		URL url = new URL(requestUrl + "?username=" + USERNAME + "&password=" + PASSWORD);

		// Open connection and read response
		HttpURLConnection connection = (HttpURLConnection) url.openConnection();
		connection.setRequestMethod("GET");
		connection.setConnectTimeout(5000); // Timeout in milliseconds
		connection.setReadTimeout(5000);    // Timeout for reading data

		BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
		String inputLine;
		StringBuilder response = new StringBuilder();

		while ((inputLine = in.readLine()) != null) {
			response.append(inputLine);
		}
		in.close();

		// Assuming the response contains the M3U URL as a plain string or in JSON
		return response.toString().trim();  // Parse it according to API response
	}

	// Function to download the M3U file
	public static void downloadM3UFile(String m3uUrl) throws IOException {
		URL url = new URL(m3uUrl);
		ReadableByteChannel readableByteChannel = Channels.newChannel(url.openStream());

		// Define the path to save the file
		FileOutputStream fileOutputStream = new FileOutputStream("downloaded_playlist.m3u");
		FileChannel fileChannel = fileOutputStream.getChannel();

		// Transfer data from the URL to the file
		fileChannel.transferFrom(readableByteChannel, 0, Long.MAX_VALUE);

		System.out.println("M3U file downloaded successfully.");
	}
}


