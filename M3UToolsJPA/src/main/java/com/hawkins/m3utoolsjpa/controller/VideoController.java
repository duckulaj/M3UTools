package com.hawkins.m3utoolsjpa.controller;

import java.io.BufferedInputStream;
import java.io.IOException;
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
import com.hawkins.m3utoolsjpa.regex.Patterns;
import com.hawkins.m3utoolsjpa.service.StreamingService;
import com.hawkins.m3utoolsjpa.utils.NetUtils;
import com.hawkins.m3utoolsjpa.utils.Utils;

@Controller
public class VideoController {

	@Autowired
	M3UItemRepository m3uItemRepository;
	
	@Autowired
	StreamingService service;

	@GetMapping(value ="stream", params = { "name" })
	public ModelAndView stream(ModelMap model, @RequestParam String name) {

		URL url = null;

		try {
			url = new URI(Utils.getURLFromName(name, m3uItemRepository)).toURL();
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (URISyntaxException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		model.addAttribute("streamUrl", url);
		model.addAttribute("filmTitle", Utils.removeFromString(name, Patterns.STRIP_COUNTRY_IDENTIFIER));

		return new ModelAndView("stream", model);


	}


	@GetMapping(value = "media")
	@ResponseBody
	public final ResponseEntity<InputStreamResource> retrieveResource(@RequestParam String streamUrl, @RequestHeader HttpHeaders headers) throws Exception {

		// HttpHeaders headers = new HttpHeaders();
		// InputStream targetStream = service.getInputStream(streamUrl, headers);
		
		
		Long contentLength = NetUtils.getContentSizeFromUrl(streamUrl);
		
		NetUtils.printHeaders(headers);
		
		HttpURLConnection con = (HttpURLConnection) new URI(streamUrl).toURL().openConnection(); 
	    
		BufferedInputStream bufferedInputStream = new BufferedInputStream(con.getInputStream());
		
		// InputStream targetStream = con.getInputStream();

	    List<HttpRange> rangeList = headers.getRange();
	    HttpRange range = rangeList.get(0);
	    long start = range.getRangeStart(contentLength);
	    long end = range.getRangeEnd(contentLength);
	    
	    headers.setContentType(MediaType.valueOf("video/mp4"));
	    headers.set("Accept-Ranges", "bytes");
	    headers.set("Expires", "0");
	    headers.set("Cache-Control", "no-cache, no-store");
	    headers.set("Connection", "keep-alive");
	    headers.set("Content-Transfer-Encoding", "binary");
	    headers.setContentLength(contentLength);
	    headers.set("range", "bytes=" + start + "-" + (end) + "/" + contentLength);

	    return new ResponseEntity<>(new InputStreamResource(bufferedInputStream), headers, HttpStatus.OK);

	}
	
	@GetMapping(value = "video")
	@ResponseBody
	public ResponseEntity<byte[]> video(@RequestParam String streamUrl, @RequestHeader(value = "Range",required = false) String range) throws IOException {
        
		long contentSize = NetUtils.getContentSizeFromUrl(streamUrl).longValue();
		long rangeStart = Long.parseLong(service.getRangeStart(range));
		long rangeEnd = Long.parseLong(service.getRangeEnd(range));
		
		if (range == null) {
			
			byte[] rangeData = null;
			try {
				rangeData = service.readByteRange(streamUrl, rangeStart, rangeEnd);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (URISyntaxException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
            return ResponseEntity.status(HttpStatus.OK)
                    .header("Content-Type", "video/mp4")
                    .header("Accept-Ranges", "bytes")
                    .header("Content-Length", String.valueOf(contentSize))
                    .body(rangeData);
        }
		
		contentSize = 128;
		
        byte[] data = service.getVideo(streamUrl, range, contentSize);

        return  ResponseEntity.status(HttpStatus.PARTIAL_CONTENT)
                .header("Content-Type", "video/mp4")
                .header("Accept-Ranges", "bytes")
                .header("Content-Length", String.valueOf(contentSize))
                .header("Content-Range", "bytes" + " " + String.valueOf(rangeStart) + "-" + String.valueOf(rangeEnd) + "/" + String.valueOf(contentSize))
                .body(data);
	

	}
	
}