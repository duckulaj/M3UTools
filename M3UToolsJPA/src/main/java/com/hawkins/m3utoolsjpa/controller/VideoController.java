package com.hawkins.m3utoolsjpa.controller;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpRange;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.servlet.ModelAndView;

import com.hawkins.m3utoolsjpa.component.ProcessManager;
import com.hawkins.m3utoolsjpa.data.M3UItemRepository;
import com.hawkins.m3utoolsjpa.properties.DownloadProperties;
import com.hawkins.m3utoolsjpa.regex.Patterns;
import com.hawkins.m3utoolsjpa.regex.RegexUtils;
import com.hawkins.m3utoolsjpa.service.StreamingService;
import com.hawkins.m3utoolsjpa.utils.FileDownloader;
import com.hawkins.m3utoolsjpa.utils.NetUtils;
import com.hawkins.m3utoolsjpa.utils.Utils;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Controller
public class VideoController {

	@Autowired
	M3UItemRepository m3uItemRepository;

	@Autowired
	StreamingService service;

	private static DownloadProperties dp = DownloadProperties.getInstance();

	@GetMapping(value ="stream", params = { "streamName" })
	public ModelAndView stream(ModelMap model, @RequestParam String streamName) {
		log.info("Received GET /stream request with streamName: {}", streamName);

		URL url = null;

		try {
			String urlString = Utils.getURLFromName(streamName, m3uItemRepository);
			if (urlString == null) {
				log.error("URL not found for streamName: " + streamName);
				return new ModelAndView("error", model);
			}
			url = new URI(Utils.getURLFromName(streamName, m3uItemRepository)).toURL();
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (URISyntaxException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		model.addAttribute("streamUrl", url);
		model.addAttribute("filmTitle", RegexUtils.removeCountryIdentifier(Utils.removeFromString(streamName, Patterns.STRIP_COUNTRY_IDENTIFIER),dp.getIncludedCountries())) ;

		return new ModelAndView("stream", model);


	}



	@GetMapping(value = "media")
	@ResponseBody
	public final ResponseEntity<InputStreamResource> retrieveResource(@RequestParam String streamUrl, @RequestHeader HttpHeaders headers) throws Exception {
		log.info("Received GET /media request with streamUrl: {} and headers: {}", streamUrl, headers);
		// Retrieve the session ID
	    String sessionId = RequestContextHolder.currentRequestAttributes().getSessionId();
	    
		
		List<HttpRange> rangeList = headers.getRange();
		long contentLength = NetUtils.getContentSizeFromUrl(streamUrl);
		if (contentLength < 0) {
			contentLength = 0;
		}
		int bufferSize = 1024 * 1024 * 4; // 4 MB buffer size

		if (rangeList.isEmpty()) {
			// No Range header present, return the entire content
			HttpURLConnection con = (HttpURLConnection) new URI(streamUrl).toURL().openConnection();
			return ResponseEntity.ok()
					.contentType(MediaType.APPLICATION_OCTET_STREAM)
					.contentLength(contentLength)
					.body(new InputStreamResource(new BufferedInputStream(con.getInputStream())));
		} else {
			// Handle Range request
			HttpRange range = rangeList.get(0);
			long start = range.getRangeStart(contentLength);
			long end = Math.min(start + bufferSize - 1, contentLength - 1);

			HttpURLConnection con = (HttpURLConnection) new URI(streamUrl).toURL().openConnection();
			con.setRequestProperty("Range", "bytes=" + start + "-" + end);
			con.connect();

			long contentLengthRange = end - start + 1;
			if (contentLengthRange < 0) {
				contentLengthRange = 0;
			}
			HttpHeaders responseHeaders = new HttpHeaders();
			responseHeaders.setContentType(MediaType.APPLICATION_OCTET_STREAM);
			responseHeaders.set("Accept-Ranges", "bytes");
			responseHeaders.setContentLength(contentLengthRange);
			responseHeaders.set("Content-Range", "bytes " + start + "-" + end + "/" + contentLength);

			return new ResponseEntity<>(new InputStreamResource(new BufferedInputStream(con.getInputStream(), bufferSize)), responseHeaders, HttpStatus.PARTIAL_CONTENT);
		}
	}

	private String convertMkvToMp4(String inputUrl, String sessionId) throws IOException, InterruptedException {
	    // Download the file to /tmp
		
		String tempFilePath = "/tmp/input.mkv";
		FileDownloader.downloadFileInSegments(inputUrl, tempFilePath, DownloadProperties.getInstance().getBufferSize());
	    
	    // Prepare output file path
	    String outputFilePath = "/tmp/output.mp4";

	    // Add shutdown hook to clean up the temporary file if the process is killed
	    Runtime.getRuntime().addShutdownHook(new Thread(() -> {
	        try {
	            Files.deleteIfExists(Paths.get(tempFilePath));
	            log.info("Temporary file deleted: " + tempFilePath);
	        } catch (IOException e) {
	            log.error("Failed to delete temporary file: " + tempFilePath, e);
	        }
	    }));

	    try {
	        // Start FFmpeg process
	        ProcessBuilder processBuilder = new ProcessBuilder(
	            "ffmpeg",
	            "-hwaccel", "cuda", // Use GPU acceleration
	            "-i", tempFilePath, 
	            "-c:v", "h264_nvenc", // Use NVIDIA encoder
	            "-preset", "fast", // Faster encoding preset
	            "-c:a", "copy", // Copy audio without re-encoding
	            "-movflags", "faststart", // Optimize for streaming
	            "-threads", "4", // Explicitly set thread count
	            outputFilePath
	        );

	        processBuilder.redirectErrorStream(true);
	        Process process = processBuilder.start();
	        ProcessManager.addProcess(sessionId, process);
	        log.info("FFmpeg process started with PID: " + process.pid());

	        // Capture and log progress
	        try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
	            String line;
	            while ((line = reader.readLine()) != null) {
	                log.info("FFmpeg Output: " + line);
	            }
	        }

	        int exitCode = process.waitFor();
	        ProcessManager.terminateProcess(sessionId);
	        log.info("FFmpeg process exited with code: " + exitCode);

	        if (exitCode != 0) {
	            throw new IOException("FFmpeg conversion failed");
	        }

	        return outputFilePath;
	    } finally {
	        // Ensure the temporary file is deleted
	        Files.deleteIfExists(Paths.get(tempFilePath));
	        log.info("Temporary file deleted: " + tempFilePath);
	    }
	}



}