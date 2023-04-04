package com.hawkins.m3utoolsjpa.service;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.hawkins.m3utoolsjpa.data.M3UItem;
import com.hawkins.m3utoolsjpa.data.M3UItemRepository;
import com.hawkins.m3utoolsjpa.epg.XmltvDoc;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class CompletableFutureService {

	@Autowired
	M3UItemRepository itemRepository;
	
	@Autowired 
	EpgService epgService;
	
	public void writeEPG() {
		
		CompletableFuture<List<M3UItem>> m3UItems = CompletableFuture.supplyAsync(() -> 
		
				itemRepository.findTvChannelsBySelected(true)
		
		);
		
		CompletableFuture<XmltvDoc> xmlTvDoc = CompletableFuture.supplyAsync(() -> 
		
			epgService.getXmlTvDoc()
		
		);
		
		
		
		
		
		
			
		CompletableFuture<Void> combinedFuture = CompletableFuture.allOf(m3UItems, xmlTvDoc);
		try {
			combinedFuture.get();
		} catch (InterruptedException | ExecutionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		if (m3UItems.isDone() && xmlTvDoc.isDone()) {
			
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
}
