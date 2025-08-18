package com.hawkins.m3utoolsjpa.redis;

import com.hawkins.m3utoolsjpa.data.M3UItem;
import com.hawkins.m3utoolsjpa.data.M3UItemRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class M3UItemCacheInitializer {
    @Autowired
    private M3UItemRepository m3UItemRepository;
    @Autowired
    private M3UItemRedisService m3UItemRedisService;

    @Bean
    public CommandLineRunner cacheM3UItemsAtStartup() {
        return args -> {
            // Purge all M3UItems from Redis before repopulating
            m3UItemRedisService.deleteAll();
            List<M3UItem> items = m3UItemRepository.findAll();
            for (M3UItem item : items) {
                m3UItemRedisService.save(item);
            }
        };
    }
}