package com.hawkins.m3utoolsjpa.controller;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import com.hawkins.m3utoolsjpa.data.FilterRepository;
import com.hawkins.m3utoolsjpa.data.M3UGroupRepository;
import com.hawkins.m3utoolsjpa.data.M3UItem;
import com.hawkins.m3utoolsjpa.data.M3UItemRepository;
import com.hawkins.m3utoolsjpa.data.SelectedChannelsRepository;
import com.hawkins.m3utoolsjpa.data.SelectedTvChannels;
import com.hawkins.m3utoolsjpa.job.DownloadJob;
import com.hawkins.m3utoolsjpa.m3u.M3UGroupSelected;
import com.hawkins.m3utoolsjpa.properties.ConfigProperty;
import com.hawkins.m3utoolsjpa.search.MovieDb;
import com.hawkins.m3utoolsjpa.service.M3UService;
import com.hawkins.m3utoolsjpa.utils.Constants;
import com.hawkins.m3utoolsjpa.utils.FileUtilsForM3UToolsJPA;

@Controller
public class M3UController {

	@Autowired
	M3UItemRepository itemRepository;

	@Autowired
	M3UGroupRepository groupRepository;

	@Autowired
	SelectedChannelsRepository tvRepo;
	
	@Autowired
	FilterRepository filterRepository;

	private M3UService m3uService;

	// @Autowired
	M3UController(M3UService m3uService) {
		this.m3uService = m3uService;
	}

	@GetMapping("/resetDatabase")
	public ModelAndView resetDatabase(ModelMap model) {

		List<M3UItem> items = new ArrayList<M3UItem>();
		m3uService.resetDatabase(itemRepository, groupRepository);
		tvRepo.save(new SelectedTvChannels((long) 1, "Test"));

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

			model.addAttribute(Constants.MOVIEDB, MovieDb.getInstance());
			model.addAttribute(Constants.SELECTEDGROUP, M3UService.getSelectedGroup(groupId, groupRepository));
			model.addAttribute("groupId", groupId);
			model.addAttribute("groups", M3UService.getM3UGroups(groupRepository));
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

		model.addAttribute("logFile", FileUtilsForM3UToolsJPA.fileTail("M3UToolsJPA.log", 100));
		return Constants.VIEW_LOG;
	}

	@GetMapping("/properties")
	public String getProperties(Model model) {

		model.addAttribute("properties", M3UService.getOrderedPropertiesEntrySet());
		model.addAttribute("configProperty", new ConfigProperty());
		model.addAttribute("propertyFile", M3UService.getConfigFileName());
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

	@GetMapping(value = "/showStatus")
	public String showStatus(Model model) {

		model.addAttribute(Constants.JOBLIST, new LinkedList<DownloadJob>());
		return Constants.STATUS;
	}

	@GetMapping(value = "/search")
	public String search(Model model, @RequestParam(required = false, defaultValue = "title") String searchType,
			@RequestParam(required = false, defaultValue = "") String criteria) {

		model.addAttribute("items", M3UService.searchMedia(searchType, criteria, itemRepository));
		model.addAttribute(Constants.MOVIEDB, MovieDb.getInstance());
		return Constants.SEARCH;
	}
	
	@GetMapping(value = "/filters")
	public String filters(Model model) {
		
		model.addAttribute("filters", M3UService.getFilters(filterRepository));		
		return Constants.FILTERS;
	}

	@GetMapping(value = "newFilter")
	public String newFilter(Model model) {
		
		return Constants.ITEMS;
		
	}
}
