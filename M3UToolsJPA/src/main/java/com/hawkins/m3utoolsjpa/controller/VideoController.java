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
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
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
import com.hawkins.m3utoolsjpa.utils.Range;
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

		// HttpHeaders headers = new HttpHeaders();
		// InputStream targetStream = service.getInputStream(streamUrl, headers);
		
		
		Long contentLength = NetUtils.getContentSizeFromUrl(streamUrl);
		
		NetUtils.printHeaders(headers);
		
		HttpURLConnection con = (HttpURLConnection) new URI(streamUrl).toURL().openConnection(); 
	    
		

	    List<HttpRange> rangeList = headers.getRange();
	    HttpRange range = rangeList.get(0);
	    long start = range.getRangeStart(contentLength);
	    long end = range.getRangeEnd(contentLength);
	    	    
	    headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
	    headers.set("Accept-Ranges", "bytes");
	    headers.set("Expires", "0");
	    headers.set("Cache-Control", "no-cache, no-store");
	    headers.set("Connection", "keep-alive");
	    headers.set("Content-Transfer-Encoding", "binary");
	    headers.setContentLength(contentLength);
	    headers.set("range", "bytes=" + start + "-" + (end) + "/" + contentLength);

	    log.info("URL is {}", streamUrl);
	    log.info("Content Length is {}", contentLength);
	    log.info("Content Type is {}", NetUtils.getContentTypeFromUrl(streamUrl));
	    
	    return new ResponseEntity<>(new InputStreamResource(new BufferedInputStream(con.getInputStream())), headers, HttpStatus.OK);

	}
	
	@GetMapping(value = "videoOld")
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
	
	@GetMapping("video")
    public ResponseEntity<Resource> streamVideo(@RequestParam String streamName,
                                                @RequestHeader(value = "Range", required = false) String rangeHeader) {
        try {
            
        	
            Resource resource = null;
			try {
				resource = new UrlResource(new URI(Utils.getURLFromName(streamName, m3uItemRepository)).toURL());
			} catch (URISyntaxException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

            if (!resource.exists() || !resource.isReadable()) {
                return ResponseEntity.notFound().build();
            }

            // Get total file size
            long fileSize = 0;
			try {
				fileSize = resource.contentLength();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

            // Parse range header
            Range range = Range.parse(rangeHeader, fileSize, resource);
            

            // Determine content range
            String contentRange = range.getContentRangeHeader();

            // Send appropriate partial content with 206 status code
            return ResponseEntity.status(HttpStatus.PARTIAL_CONTENT)
                    .contentType(MediaType.valueOf("video/mp4"))
                    .header(HttpHeaders.CONTENT_RANGE, contentRange)
                    .body(range.getResource());
        } catch (MalformedURLException e) {
            e.printStackTrace();
            return ResponseEntity.notFound().build();
        }
    }
}
	
