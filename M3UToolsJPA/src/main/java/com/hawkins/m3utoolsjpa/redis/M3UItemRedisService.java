package com.hawkins.m3utoolsjpa.redis;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Service;

import com.hawkins.m3utoolsjpa.data.M3UItem;

@Service
public class M3UItemRedisService {
    private static final String PREFIX = "m3uitem:";

    @Autowired
    private RedisTemplate<String, M3UItem> redisTemplate;

    public void save(M3UItem item) {
        ValueOperations<String, M3UItem> ops = redisTemplate.opsForValue();
        ops.set(PREFIX + item.getId(), item);
    }

    public M3UItem findById(Long id) {
        ValueOperations<String, M3UItem> ops = redisTemplate.opsForValue();
        return ops.get(PREFIX + id);
    }

    public void delete(Long id) {
        redisTemplate.delete(PREFIX + id);
    }

    public void deleteAll() {
        Set<String> keys = redisTemplate.keys(PREFIX + "*");
        if (keys != null && !keys.isEmpty()) {
            redisTemplate.delete(keys);
        }
    }

    public List<M3UItem> findAll() {
        Set<String> keys = redisTemplate.keys(PREFIX + "*");
        List<M3UItem> items = new ArrayList<>();
        if (keys != null) {
            for (String key : keys) {
                M3UItem item = redisTemplate.opsForValue().get(key);
                if (item != null) {
                    items.add(item);
                }
            }
        }
        return items;
    }
}