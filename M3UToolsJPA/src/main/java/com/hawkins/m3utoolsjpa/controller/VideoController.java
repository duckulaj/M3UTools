package com.hawkins.m3utoolsjpa.controller;

import java.io.BufferedInputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
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
import org.springframework.web.servlet.ModelAndView;

import com.hawkins.m3utoolsjpa.data.M3UItemRepository;
import com.hawkins.m3utoolsjpa.properties.DownloadProperties;
import com.hawkins.m3utoolsjpa.regex.Patterns;
import com.hawkins.m3utoolsjpa.regex.RegexUtils;
import com.hawkins.m3utoolsjpa.service.StreamingService;
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

		URL url = null;

		try {
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
		List<HttpRange> rangeList = headers.getRange();
		long contentLength = NetUtils.getContentSizeFromUrl(streamUrl);
		int bufferSize = 1024 * 1024; // 1 MB buffer size

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
			HttpHeaders responseHeaders = new HttpHeaders();
			responseHeaders.setContentType(MediaType.APPLICATION_OCTET_STREAM);
			responseHeaders.set("Accept-Ranges", "bytes");
			responseHeaders.setContentLength(contentLengthRange);
			responseHeaders.set("Content-Range", "bytes " + start + "-" + end + "/" + contentLength);

			return new ResponseEntity<>(new InputStreamResource(new BufferedInputStream(con.getInputStream(), bufferSize)), responseHeaders, HttpStatus.PARTIAL_CONTENT);
		}
	}

}

