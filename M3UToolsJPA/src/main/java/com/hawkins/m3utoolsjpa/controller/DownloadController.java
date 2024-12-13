package com.hawkins.m3utoolsjpa.controller;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.concurrent.CompletableFuture;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import com.hawkins.m3utoolsjpa.data.M3UItemRepository;
import com.hawkins.m3utoolsjpa.properties.DownloadProperties;
import com.hawkins.m3utoolsjpa.service.DownloadService;
import com.hawkins.m3utoolsjpa.utils.NetUtils;
import com.hawkins.m3utoolsjpa.utils.Utils;

import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;

@Controller
@Slf4j
public class DownloadController {

	@Autowired
	M3UItemRepository itemRepository;
	
	@Autowired
	DownloadService downloadService;
	
	@GetMapping(value ="newDownload", params = { "downloadName" })
    public void newDownload(@RequestParam String downloadName, HttpServletResponse response) throws IOException {
        
		Long contentSize = 0L;
        URL url = null;
		try {
			log.info("downloadName is {}", downloadName);
			String storedUrl = Utils.getURLFromName(downloadName, itemRepository); 
			log.info("StoredUrl is {}", storedUrl);
			url = new URI(storedUrl).toURL();
			
			log.info("Downloading {}", downloadName);
			contentSize = NetUtils.getContentSizeFromUrl(url);
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (URISyntaxException e) {
			e.printStackTrace();
		} catch (NullPointerException npe) {
			npe.printStackTrace();
		}
        
		int bufferSize = DownloadProperties.getInstance().getBufferSize();
        BufferedInputStream inputStream = new BufferedInputStream(url.openStream(), bufferSize);
        // BufferedInputStream inputStream = new BufferedInputStream(url.openStream());

        response.setContentType("application/octet-stream");
        response.setHeader("Content-Disposition", "attachment; filename=" + downloadName + ".mp4");
        response.setHeader("Content-Length", contentSize.toString());
        response.addHeader(HttpHeaders.CONNECTION, "keep-alive");
        // response.addHeader("Pragma", "no-cache");
        // response.addHeader("Cache-Control", "no-cache");

        int bytesRead;
        OutputStream out = response.getOutputStream();
        BufferedOutputStream bos = new BufferedOutputStream(out, bufferSize);
        while ((bytesRead = inputStream.read()) != -1) {
            // response.getOutputStream().write(bytesRead);
        	bos.write(bytesRead);
        }

        inputStream.close();
        bos.close();
        out.close();
    }
	
	@GetMapping(value ="downloadToServer", params = { "name" })
    public ModelAndView downloadToServer(@RequestParam String name, ModelMap model) throws IOException {
    
		CompletableFuture<Void> downloaded = CompletableFuture.runAsync(() -> 
			downloadService.downloadToserver(name)
		);
		
		return new ModelAndView("forward:/", model);
	}
}
