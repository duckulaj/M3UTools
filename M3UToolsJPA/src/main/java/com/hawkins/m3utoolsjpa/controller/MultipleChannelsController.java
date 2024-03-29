package com.hawkins.m3utoolsjpa.controller;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.hawkins.m3utoolsjpa.data.SelectedChannel;
import com.hawkins.m3utoolsjpa.data.SelectedChannelsCreationDto;
import com.hawkins.m3utoolsjpa.service.EpgService;
import com.hawkins.m3utoolsjpa.service.M3UService;
import com.hawkins.m3utoolsjpa.service.SelectedChannelService;
import com.hawkins.m3utoolsjpa.utils.Constants;


@Controller
public class MultipleChannelsController {

    @Autowired
    private SelectedChannelService channelsService;
    
    @Autowired
    private M3UService m3uService;
    
    @Autowired
    private EpgService epgService;

    @GetMapping(value = "/tvChannels")
    public String editTvChannels(Model model, @RequestParam(required = false, defaultValue = "-1") Long groupId) {
        
    	
    	List<SelectedChannel> channels = new ArrayList<>();
        channelsService.find(groupId)
            .iterator()
            .forEachRemaining(channels::add);

        model.addAttribute(Constants.SELECTEDGROUP, m3uService.getSelectedGroup(groupId));
        model.addAttribute("groupId", groupId);
        // model.addAttribute("groups", m3uService.getM3UGroups());
        model.addAttribute("groups", m3uService.getM3UGroupsByType(Constants.LIVE));
        model.addAttribute("form", new SelectedChannelsCreationDto(channels));

        return Constants.EDIT_CHANNELS;
    }

    @PostMapping(value = "/saveTvChannels")
    public String saveTvChannels(@ModelAttribute SelectedChannelsCreationDto form, Model model) {
        channelsService.saveAll(form.getChannels());
        m3uService.writeTvChannelsM3U();
        epgService.readEPG();

        model.addAttribute("channels", channelsService.find(-1L));

        return "redirect:/tvChannels";
    }
    
    @GetMapping(value = "/showAllSelected")
    public String showAllSelected(Model model, @RequestParam(required = false, defaultValue = "-1") Long groupId) {
    	
    	List<SelectedChannel> channels = new ArrayList<>();
        channelsService.find(true)
            .iterator()
            .forEachRemaining(channels::add);

        model.addAttribute(Constants.SELECTEDGROUP, m3uService.getSelectedGroup(groupId));
        model.addAttribute("groups", m3uService.getM3UGroups());
        model.addAttribute("groupId", groupId);
        model.addAttribute("form", new SelectedChannelsCreationDto(channels));
        
        
        return Constants.EDIT_CHANNELS;
    	
    }
}
