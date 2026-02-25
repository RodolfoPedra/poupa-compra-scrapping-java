package br.com.poupacompra.scraping.service;

import java.util.Map;

import org.springframework.cache.CacheManager;
import org.springframework.cache.caffeine.CaffeineCache;
import org.springframework.stereotype.Service;

import com.github.benmanes.caffeine.cache.Cache;

import br.com.poupacompra.scraping.config.CacheConfig;
import br.com.poupacompra.scraping.config.ScrapingProperties;

@Service
public class CacheService {
    
    private final CacheManager cacheManager;
    private final ScrapingProperties properties;
    
    public CacheService(CacheManager cacheManager, ScrapingProperties properties) {
        this.cacheManager = cacheManager;
        this.properties = properties;
    }
    
    public long clearCache() {
        org.springframework.cache.Cache springCache = cacheManager.getCache(CacheConfig.NFE_CACHE);
        if (springCache instanceof CaffeineCache caffeineCache) {
            Cache<Object, Object> nativeCache = caffeineCache.getNativeCache();
            long size = nativeCache.estimatedSize();
            nativeCache.invalidateAll();
            return size;
        }
        return 0;
    }
    
    public Map<String, Object> getCacheStats() {
        org.springframework.cache.Cache springCache = cacheManager.getCache(CacheConfig.NFE_CACHE);
        
        long totalEntries = 0;
        long hitCount = 0;
        long missCount = 0;
        double hitRate = 0;
        
        if (springCache instanceof CaffeineCache caffeineCache) {
            Cache<Object, Object> nativeCache = caffeineCache.getNativeCache();
            totalEntries = nativeCache.estimatedSize();
            
            var stats = nativeCache.stats();
            hitCount = stats.hitCount();
            missCount = stats.missCount();
            hitRate = stats.hitRate();
        }
        
        return Map.of(
            "totalEntries", totalEntries,
            "hitCount", hitCount,
            "missCount", missCount,
            "hitRate", String.format("%.2f%%", hitRate * 100),
            "ttlHours", properties.getCache().getTtlHours(),
            "maxSize", properties.getCache().getMaxSize()
        );
    }
    
    public long getCacheSize() {
        org.springframework.cache.Cache springCache = cacheManager.getCache(CacheConfig.NFE_CACHE);
        if (springCache instanceof CaffeineCache caffeineCache) {
            return caffeineCache.getNativeCache().estimatedSize();
        }
        return 0;
    }
}
