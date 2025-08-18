package com.hawkins.m3utoolsjpa.redis;

import com.hawkins.m3utoolsjpa.data.M3UGroup;
import com.hawkins.m3utoolsjpa.data.M3UGroupRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class M3UGroupCacheInitializer {
    @Autowired
    private M3UGroupRepository m3UGroupRepository;
    @Autowired
    private M3UGroupRedisService m3UGroupRedisService;

    @Bean
    public CommandLineRunner cacheM3UGroupsAtStartup() {
        return args -> {
            // Purge all M3UGroups from Redis before repopulating
            m3UGroupRedisService.deleteAll();
            List<M3UGroup> groups = m3UGroupRepository.findAll();
            for (M3UGroup group : groups) {
                m3UGroupRedisService.save(group);
            }
        };
    }
}