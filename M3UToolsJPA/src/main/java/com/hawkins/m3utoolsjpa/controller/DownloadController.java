package com.hawkins.m3utoolsjpa.controller;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.hawkins.m3utoolsjpa.data.M3UItemRepository;
import com.hawkins.m3utoolsjpa.properties.DownloadProperties;
import com.hawkins.m3utoolsjpa.utils.NetUtils;
import com.hawkins.m3utoolsjpa.utils.Utils;

import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;

@Controller
@Slf4j
public class DownloadController {

	@Autowired
	M3UItemRepository itemRepository;
	
	@GetMapping(value ="newDownload", params = { "name" })
    public void newDownload(@RequestParam String name, HttpServletResponse response) throws IOException {
        
		Long contentSize = 0L;
        URL url = null;
		try {
			url = new URI(Utils.getURLFromName(name, itemRepository)).toURL();
			log.info("Downloading {}", name);
			contentSize = NetUtils.getContentSizeFromUrl(url);
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (URISyntaxException e) {
			e.printStackTrace();
		}
        
		int bufferSize = DownloadProperties.getInstance().getBufferSize();
        BufferedInputStream inputStream = new BufferedInputStream(url.openStream(), bufferSize);

        response.setContentType("application/octet-stream");
        response.setHeader("Content-Disposition", "attachment; filename=" + name + ".mp4");
        response.setHeader("Content-Length", contentSize.toString());
        response.addHeader(HttpHeaders.CONNECTION, "keep-alive");
        // response.addHeader("Pragma", "no-cache");
        // response.addHeader("Cache-Control", "no-cache");

        
        int bytesRead;
        while ((bytesRead = inputStream.read()) != -1) {
            response.getOutputStream().write(bytesRead);
        }

        inputStream.close();
    }
}
