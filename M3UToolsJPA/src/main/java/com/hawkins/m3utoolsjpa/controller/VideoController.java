package com.hawkins.m3utoolsjpa.controller;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.hawkins.m3utoolsjpa.data.M3UItemRepository;
import com.hawkins.m3utoolsjpa.regex.Patterns;
import com.hawkins.m3utoolsjpa.utils.Utils;

@Controller
public class VideoController {

	@Autowired
	M3UItemRepository m3uItemRepository;

	@GetMapping(value ="stream", params = { "name" })
    public String stream(Model model, @RequestParam String name) throws IOException {
                
        URL url = null;
		try {
			url = new URI(Utils.getURLFromName(name, m3uItemRepository)).toURL();
			model.addAttribute("streamUrl", url);
			model.addAttribute("filmTitle", Utils.removeFromString(name, Patterns.STRIP_COUNTRY_IDENTIFIER));
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (URISyntaxException e) {
			e.printStackTrace();
		}
        return "stream";
	}
}
