package com.hawkins.m3utoolsjpa.redis;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Service;

import com.hawkins.m3utoolsjpa.data.M3UGroup;

@Service
public class M3UGroupRedisService {
    private static final String PREFIX = "m3ugroup:";

    @Autowired
    private RedisTemplate<String, M3UGroup> redisTemplate;

    public void save(M3UGroup group) {
        ValueOperations<String, M3UGroup> ops = redisTemplate.opsForValue();
        ops.set(PREFIX + group.getId(), group);
    }

    public M3UGroup findById(Long id) {
        ValueOperations<String, M3UGroup> ops = redisTemplate.opsForValue();
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

    public List<M3UGroup> findAll() {
        Set<String> keys = redisTemplate.keys(PREFIX + "*");
        List<M3UGroup> groups = new ArrayList<>();
        if (keys != null) {
            for (String key : keys) {
                M3UGroup group = redisTemplate.opsForValue().get(key);
                if (group != null) {
                    groups.add(group);
                }
            }
        }
        return groups;
    }
}
