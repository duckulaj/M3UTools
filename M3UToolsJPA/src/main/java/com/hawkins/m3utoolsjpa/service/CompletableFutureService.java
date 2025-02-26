package com.hawkins.m3utoolsjpa.service;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.hawkins.m3utoolsjpa.annotations.TrackExecutionTime;
import com.hawkins.m3utoolsjpa.data.M3UGroupRepository;
import com.hawkins.m3utoolsjpa.data.M3UItem;
import com.hawkins.m3utoolsjpa.data.M3UItemRepository;
import com.hawkins.m3utoolsjpa.epg.XmltvDoc;
import com.hawkins.m3utoolsjpa.parser.Parser;
import com.hawkins.m3utoolsjpa.utils.Utils;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class CompletableFutureService {

	@Autowired
	M3UItemRepository itemRepository;

	@Autowired
	M3UGroupRepository groupRepository;

	@Autowired 
	EpgService epgService;

	@Autowired
	DatabaseService databaseService;

	@Autowired
	ParserService parserService;

	@TrackExecutionTime
	public void writeEPG() {

		CompletableFuture<List<M3UItem>> m3UItems = CompletableFuture.supplyAsync(() -> 
		itemRepository.findTvChannelsBySelected(true)
				);

		CompletableFuture<XmltvDoc> xmlTvDoc = CompletableFuture.supplyAsync(() -> 
		epgService.getXmlTvDoc()
				);

		CompletableFuture<List<M3UItem>> m3UItemsFromParser = CompletableFuture.supplyAsync(() -> 
		Parser.parse()

				);

		CompletableFuture<Void> combinedFuture = CompletableFuture.allOf(m3UItemsFromParser, m3UItems, xmlTvDoc);

		try {
			log.info("Starting combinedFuture.get() at {}", Utils.printNow());
			combinedFuture.get();
		} catch (InterruptedException | ExecutionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		if (combinedFuture.isDone())
			log.info("combinedFuture.isDone() at {}", Utils.printNow());
		try {
			log.info("Number of M3UItems = {}", m3UItems.get().size());
		} catch (InterruptedException | ExecutionException e) {
			e.printStackTrace();
		}

		log.info("CompletableFutures completed");
	}

	public void reloadDatabase() {


		log.info("Starting m3UItemsFromParser at {}", Utils.printNow());
		parserService.parseM3UFile();

	}

	public void cleanItemsAndGroups() {

		log.info("Starting cleanItemsAndGroups at {}", Utils.printNow());

		CompletableFuture<Void> cleanItemsAndGroups = CompletableFuture.runAsync(() -> {
			databaseService.deleteItemsAndGroups();
		});

		try {
			log.info("Starting cleanItemsAndGroups.get() at {}", Utils.printNow());
			cleanItemsAndGroups.get();
		} catch (InterruptedException | ExecutionException e) {
			log.info("Interrupted Execution - {}", e.getMessage());
		}

		if (cleanItemsAndGroups.isDone()) log.info("cleanItemsAndGroups.isDone() at {}", Utils.printNow());
		log.info("CompletableFutures completed");
	}

}
