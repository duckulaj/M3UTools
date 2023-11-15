package com.hawkins.m3utoolsjpa.controller;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.ui.ModelMap;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import com.hawkins.m3utoolsjpa.data.Filter;
import com.hawkins.m3utoolsjpa.data.M3UItem;
import com.hawkins.m3utoolsjpa.exception.M3UItemsNotFoundException;
import com.hawkins.m3utoolsjpa.m3u.M3UGroupSelected;
import com.hawkins.m3utoolsjpa.properties.ConfigProperty;
import com.hawkins.m3utoolsjpa.search.MovieDb;
import com.hawkins.m3utoolsjpa.service.M3UService;
import com.hawkins.m3utoolsjpa.utils.Constants;
import com.hawkins.m3utoolsjpa.utils.FileUtilsForM3UToolsJPA;
import com.hawkins.m3utoolsjpa.utils.LoggerUtils;

import jakarta.validation.Valid;

@Controller
public class M3UController {

	@Autowired
	M3UService m3uService;
	
	@GetMapping("/resetDatabase")
	public ModelAndView resetDatabase(ModelMap model) {

		List<M3UItem> items = new ArrayList<M3UItem>();
		try {
			m3uService.resetDatabase();
		} catch (M3UItemsNotFoundException e) {
			model.addAttribute("message", e.getMessage());
			e.printStackTrace();
		}
		
		model.addAttribute("groups", m3uService.getM3UGroups());
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

			Page<M3UItem> pageItems = m3uService.getPageableItems(groupId, page, size);

			model.addAttribute(Constants.MOVIEDB, MovieDb.getInstance());
			model.addAttribute(Constants.SELECTEDGROUP, m3uService.getSelectedGroup(groupId));
			model.addAttribute("groupId", groupId);
			model.addAttribute("groups", m3uService.getM3UGroups());
			model.addAttribute("items", pageItems.getContent());
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

		model.addAttribute("logLevels", LoggerUtils.getLogLevelsAsList());
		model.addAttribute("logFile", FileUtilsForM3UToolsJPA.fileTail("M3UToolsJPA.log", 200));
		return Constants.VIEW_LOG;
	}

	@GetMapping("/properties")
	public String getProperties(Model model) {

		model.addAttribute("properties", M3UService.getOrderedPropertiesEntrySet());
		model.addAttribute("configProperty", new ConfigProperty());
		model.addAttribute("propertyFile", M3UService.getConfigFileName());
		model.addAttribute("logLevels", LoggerUtils.getLogLevelsAsList());
		return Constants.PROPERTIES;
	}

	@PostMapping(value = "/updateProperty")
	public String updateProperty(Model model, @ModelAttribute ConfigProperty configProperty) {

		M3UService.updateProperty(configProperty);
		model.addAttribute("properties", M3UService.getOrderedPropertiesEntrySet());
		model.addAttribute("configProperty", new ConfigProperty());
		model.addAttribute("propertyFile", M3UService.getConfigFileName());

		return Constants.PROPERTIES;
	}

	

	@GetMapping(value = "/search")
	public String search(Model model, @RequestParam(required = false, defaultValue = "title") 
		String searchType, @RequestParam(required = false, defaultValue = "")
		String genre, @RequestParam(required = false, defaultValue = "")
		String criteria) {

		if (searchType.equals("genre")) criteria = genre;
		
		model.addAttribute("items", m3uService.searchMedia(searchType, criteria));
		model.addAttribute("genres", m3uService.getGenres());
		model.addAttribute(Constants.MOVIEDB, MovieDb.getInstance());
		return Constants.SEARCH;
	}
	
	@GetMapping(value = "/filters")
	public String filters(Model model) {
		
		model.addAttribute("filters", m3uService.getFilters());		
		return Constants.FILTERS;
	}
	
	@GetMapping(value = "/newFilter")
	public String newFilter(Model model) {
		
		model.addAttribute("groups", m3uService.getM3UGroupsByType(Constants.LIVE));
		model.addAttribute("filter", new Filter(null, null, null, null, null));
		return Constants.ADD_FILTER;
	
	}

	@PostMapping("/saveFilter")
    public String saveFilter(@Valid Filter filter, BindingResult result, Model model) {
        if (result.hasErrors()) {
            return Constants.ADD_FILTER;
        }
        
        
        m3uService.saveFilter(filter);
        
        return "redirect:/" + Constants.FILTERS;
    }
}
