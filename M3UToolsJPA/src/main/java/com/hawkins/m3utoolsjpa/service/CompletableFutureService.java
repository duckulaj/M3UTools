package com.hawkins.m3utoolsjpa.service;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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
	EpgService epgService;

	public void writeEPG() {

	    


		log.info("Starting m3UItems at {}", Utils.printNow());
		CompletableFuture<List<M3UItem>> m3UItems = CompletableFuture.supplyAsync(() -> 

		itemRepository.findTvChannelsBySelected(true)

				);

		log.info("Starting xmlTvDoc at {}", Utils.printNow());
		CompletableFuture<XmltvDoc> xmlTvDoc = CompletableFuture.supplyAsync(() -> 

		epgService.getXmlTvDoc()

				);
		
		log.info("Starting m3UItemsFromParser at {}", Utils.printNow());
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
			// if (m3UItemsFromParser.isDone() && m3UItems.isDone() && xmlTvDoc.isDone()) {
			log.info("combinedFuture.isDone() at {}", Utils.printNow());
		try {
			log.info("Number of M3UItems = {}", m3UItems.get().size());
		} catch (InterruptedException | ExecutionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		/* 
		 * Now we can process everything together
		 * CompletableFuture for iterating through Channels
		 * CompletableFuture for iterating through Programmes
		 * 
		 * Look at TV Channel selection as candidate for CompltableFutures
		 */

		log.info("CompletableFutures completed");
	}

}

