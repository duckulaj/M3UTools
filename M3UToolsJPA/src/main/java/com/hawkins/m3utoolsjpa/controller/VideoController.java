package com.hawkins.m3utoolsjpa.controller;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
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
		
		NetUtils.printHeaders(headers);
		
		HttpURLConnection con = (HttpURLConnection) new URI(streamUrl).toURL().openConnection(); 
	    InputStream targetStream = con.getInputStream();

	    headers.setContentType(MediaType.valueOf("video/mp4"));
	    headers.set("Accept-Ranges", "bytes");
	    headers.set("Expires", "0");
	    headers.set("Cache-Control", "no-cache, no-store");
	    headers.set("Connection", "keep-alive");
	    headers.set("Content-Transfer-Encoding", "binary");
	    headers.setContentLength(NetUtils.getContentSizeFromUrl(streamUrl));
	    headers.set("range", "bytes=0-100000");

	    return new ResponseEntity<>(new InputStreamResource(targetStream), headers, HttpStatus.OK);

	}
	
	@GetMapping(value = "video")
	@ResponseBody
	public ResponseEntity<byte[]> video(@RequestParam String streamUrl, @RequestHeader(value = "Range",required = false) String range) throws IOException {
        
		long contentSize = NetUtils.getContentSizeFromUrl(streamUrl).longValue();
		long rangeStart = Long.parseLong(getRangeStart(range));
		long rangeEnd = Long.parseLong(getRangeEnd(range));
		
		if (range == null) {
			
			byte[] rangeData = null;
			try {
				rangeData = readByteRange(streamUrl, rangeStart, rangeEnd);
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
		
        byte[] data = getVideo(streamUrl, range, contentSize);

        return  ResponseEntity.status(HttpStatus.PARTIAL_CONTENT)
                .header("Content-Type", "video/mp4")
                .header("Accept-Ranges", "bytes")
                .header("Content-Length", String.valueOf(contentSize))
                .header("Content-Range", "bytes" + " " + getRangeStart(range) + "-" + getRangeEnd(range) + "/" + contentSize)
                .body(data);
	

	}
	
	public byte[] readByteRange(String streamUrl, long start, long end) throws IOException, URISyntaxException {
        try (InputStream inputStream = (new URI(streamUrl).toURL().openStream());
             ByteArrayOutputStream bufferedOutputStream = new ByteArrayOutputStream()) {
            byte[] data = new byte[128];
            int nRead;
            while ((nRead = inputStream.read(data, 0, data.length)) != -1) {
                bufferedOutputStream.write(data, 0, nRead);
            }
            bufferedOutputStream.flush();
            byte[] result = new byte[(int) (end - start) + 1];
            System.arraycopy(bufferedOutputStream.toByteArray(), (int) start, result, 0, result.length);
            return result;
        }
    }

	public byte[] getVideo(String streamUrl, String range, long fileSize) {
		byte[] data = null;
        
        String[] ranges = range.split("-");
        Long rangeStart = Long.parseLong(ranges[0].substring(6));
        Long rangeEnd = fileSize;
        if (ranges.length > 1) {
            rangeEnd = Long.parseLong(ranges[1]);
        } else {
            rangeEnd = fileSize - 1;
        }
        if (fileSize < rangeEnd) {
            rangeEnd = fileSize - 1;
        }
        String contentLength = String.valueOf((rangeEnd - rangeStart) + 1);
       try {
		data = readByteRange(streamUrl, rangeStart, rangeEnd);
	} catch (IOException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	} catch (URISyntaxException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
       return  data;
	}
	
	public String getRangeStart(String rangeHeader) {
		
		String rangeStart = null;
		
		if (rangeHeader != null && rangeHeader.startsWith("bytes=")) {
            String[] rangeParts = rangeHeader.substring(6).split("-");
            rangeStart = rangeParts[0];
            // Now you have the range start
            System.out.println("Range start: " + rangeStart);
        } else {
            System.out.println("Range header not found or invalid.");
        }
		
		return rangeStart;
    }
	
	public String getRangeEnd(String rangeHeader) {
		
		String rangeEnd = "10000";
		
		if (rangeHeader != null && rangeHeader.startsWith("bytes=")) {
            String[] rangeParts = rangeHeader.substring(6).split("-");
            if (rangeParts.length > 1) {
            	rangeEnd = rangeParts[1];
            	// Now you have the range start
            	System.out.println("Range end: " + rangeEnd);
            }
        } else {
            System.out.println("Range header not found or invalid.");
        }
		
		return rangeEnd;
    }


}