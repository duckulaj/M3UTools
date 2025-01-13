
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

    @GetMapping(value = "newDownload", params = { "downloadName" })
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
        } catch (MalformedURLException | URISyntaxException | NullPointerException e) {
            log.error("Error occurred while processing the URL: ", e);
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid URL");
            return;
        }

        int bufferSize = DownloadProperties.getInstance().getBufferSize();
        try (BufferedInputStream inputStream = new BufferedInputStream(url.openStream(), bufferSize);
             OutputStream out = response.getOutputStream();
             BufferedOutputStream bos = new BufferedOutputStream(out, bufferSize)) {

            response.setContentType("application/octet-stream");
            response.setHeader("Content-Disposition", "attachment; filename=" + downloadName + ".mp4");
            response.setHeader("Content-Length", contentSize.toString());
            response.addHeader(HttpHeaders.CONNECTION, "keep-alive");

            byte[] buffer = new byte[bufferSize];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                bos.write(buffer, 0, bytesRead);
            }
        } catch (IOException e) {
            log.error("Error occurred while downloading the file: ", e);
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Error downloading file");
        }
    }

    @GetMapping(value = "downloadToServer", params = { "name" })
    public ModelAndView downloadToServer(@RequestParam String name, ModelMap model) throws IOException {
        CompletableFuture.runAsync(() -> downloadService.downloadToserver(name));
        return new ModelAndView("forward:/", model);
    }
}
