package com.hawkins.m3utoolsjpa.controller;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import com.hawkins.m3utoolsjpa.data.M3UGroupRepository;
import com.hawkins.m3utoolsjpa.data.M3UItem;
import com.hawkins.m3utoolsjpa.data.M3UItemRepository;
import com.hawkins.m3utoolsjpa.m3u.M3UGroupSelected;
import com.hawkins.m3utoolsjpa.properties.ConfigProperty;
import com.hawkins.m3utoolsjpa.service.M3UService;
import com.hawkins.m3utoolsjpa.utils.Constants;
import com.hawkins.m3utoolsjpa.utils.FileUtils;

@Controller
public class M3UController {

	@Autowired
	M3UItemRepository itemRepository;

	@Autowired 
	M3UGroupRepository groupRepository;

	private M3UService m3uService;

	// @Autowired
	M3UController(M3UService m3uService) {
		this.m3uService = m3uService;
	}
	
	
	@GetMapping("/resetDatabase")
	public ModelAndView resetDatabase(ModelMap model) {

		List<M3UItem> items = new ArrayList<M3UItem>();
		m3uService.resetDatabase(itemRepository, groupRepository);

		model.addAttribute("groups", M3UService.getM3UGroups(groupRepository));
		model.addAttribute("items", items);
		model.addAttribute("name", "");
		model.addAttribute(Constants.SELECTEDGROUP, new M3UGroupSelected());
		return new ModelAndView("forward:/", model);
	}

	@GetMapping("/")
	public String getAll(Model model, @RequestParam(required = false, defaultValue = "-1") Long groupId,
			@RequestParam(defaultValue = "1") int page, @RequestParam(defaultValue = "10") int size,
			@ModelAttribute(Constants.SELECTEDGROUP) M3UGroupSelected selectedGroup) {
		try {
			
			Page<M3UItem> pageItems = M3UService.getPageableItems(groupId, page, size, itemRepository);
			
			model.addAttribute(Constants.SELECTEDGROUP, M3UService.getSelectedGroup(groupId, groupRepository));
			model.addAttribute("groupId", groupId);
			model.addAttribute("groups", M3UService.getM3UGroups(groupRepository));
			model.addAttribute("items",  pageItems.getContent());
			model.addAttribute("currentPage", pageItems.getNumber() + 1);
			model.addAttribute("totalItems", pageItems.getTotalElements());
			model.addAttribute("totalPages", pageItems.getTotalPages());
			model.addAttribute("pageSize", size);
		} catch (Exception e) {
			model.addAttribute("message", e.getMessage());
		}

		return Constants.ITEMS;
	}

	@GetMapping("/viewLog") 
	public String viewLog(Model model) {
		
		model.addAttribute("logFile", FileUtils.fileTail("M3UToolsJPA.log", 100));
		return Constants.VIEW_LOG;
	}
	
	@GetMapping("/properties")
	public String getProperties(Model model) {
		
		model.addAttribute("properties", M3UService.getOrderedPropertiesEntrySet());
		model.addAttribute("configProperty", new ConfigProperty());
		return Constants.PROPERTIES;
	}
	
	@RequestMapping(value="/updateProperty", method = RequestMethod.POST)
	public String updateProperty(Model model, @ModelAttribute ConfigProperty configProperty) {
		
		M3UService.updateProperty(configProperty);
		model.addAttribute("properties", M3UService.getOrderedPropertiesEntrySet());
		model.addAttribute("configProperty", new ConfigProperty());
		
		return Constants.PROPERTIES;
	}
	

}
