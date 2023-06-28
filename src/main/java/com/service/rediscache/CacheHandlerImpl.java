package com.service.rediscache;

import com.service.model.GlobalResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.CacheManager;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

@Component
public class CacheHandlerImpl implements CacheHandler{

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Autowired
    private CacheManager cacheManager;
    @Override
    public GlobalResponse clearCache() {
//        cacheManager.getCacheNames().forEach(cacheName -> cacheManager.getCache(cacheName).clear());
        redisTemplate.getConnectionFactory().getConnection().flushAll();
        return GlobalResponse.getSuccess("cleared");
    }
}
