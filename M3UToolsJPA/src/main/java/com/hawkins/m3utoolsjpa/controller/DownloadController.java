package com.hawkins.m3utoolsjpa.controller;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.hawkins.m3utoolsjpa.data.M3UItemRepository;
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
        
        URL url = null;
		try {
			url = new URI(Utils.getURLFromName(name, itemRepository)).toURL();
			log.info("Downloading {}", name);
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (URISyntaxException e) {
			e.printStackTrace();
		}
        
		int bufferSize = 16384;
        BufferedInputStream inputStream = new BufferedInputStream(url.openStream(), bufferSize);

        response.setContentType("application/octet-stream");
        response.setHeader("Content-Disposition", "attachment; filename=" + name + ".mp4");

        
        int bytesRead;
        while ((bytesRead = inputStream.read()) != -1) {
            response.getOutputStream().write(bytesRead);
        }

        inputStream.close();
    }
}
