package com.hawkins.m3utoolsjpa;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.data.repository.config.BootstrapMode;


@SpringBootApplication
@EnableJpaRepositories(bootstrapMode = BootstrapMode.DEFAULT)
public class M3UToolsJpaApplication {

	public static void main(String[] args) {
		SpringApplication.run(M3UToolsJpaApplication.class, args);
	}
	
	/*@Bean
	public CommandLineRunner demo(M3UItemRepository itemRepository, M3UGroupRepository groupRepository) {
		return (args) -> {
			
			for (M3UItem m3uItem : itemRepository.findAll()) {
				itemRepository.delete(m3uItem); 
			}
			
			for (M3UGroup m3uGroup : groupRepository.findAll()) {
				groupRepository.delete(m3uGroup);
			}
						
			LinkedList<M3UItem> items = Parser.parse();
			
			itemRepository.saveAll(items); 
			log.info("Saved {} M3UItem(s)", items.size());
			// List<M3UItem> groups = Queries.getGroupsFromEntryList();
			
			List<String> liveItems = itemRepository.findDistinctByType(Constants.LIVE);
			log.info("Found {} Live M3UItem(s)", liveItems.size());
			
			List<String> movieItems = itemRepository.findDistinctByType(Constants.MOVIE);
			log.info("Found {} Movie M3UItem(s)", movieItems.size());
			
			List<String> tvShowItems = itemRepository.findDistinctByType(Constants.SERIES);
			log.info("Found {} TvShow M3UItem(s)", tvShowItems.size());
			
			
			
			for (String item : liveItems) {
				groupRepository.save(new M3UGroup(item, Constants.LIVE));
			}
			
			for (String item : movieItems) {
				groupRepository.save(new M3UGroup(item, Constants.MOVIE));
			}
			
			for (String item : tvShowItems) {
				groupRepository.save(new M3UGroup(item, Constants.SERIES));
			}
		};
	}*/

	
}
