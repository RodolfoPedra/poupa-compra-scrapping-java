package br.com.poupacompra.scraping.config;

import java.util.concurrent.TimeUnit;

import org.springframework.cache.CacheManager;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.github.benmanes.caffeine.cache.Caffeine;

@Configuration
public class CacheConfig {
    
    public static final String NFE_CACHE = "nfeCache";
    
    private final ScrapingProperties scrapingProperties;
    
    public CacheConfig(ScrapingProperties scrapingProperties) {
        this.scrapingProperties = scrapingProperties;
    }
    
    @Bean
    public Caffeine<Object, Object> caffeineConfig() {
        return Caffeine.newBuilder()
            .expireAfterWrite(scrapingProperties.getCache().getTtlHours(), TimeUnit.HOURS)
            .maximumSize(scrapingProperties.getCache().getMaxSize())
            .recordStats();
    }
    
    @Bean
    public CacheManager cacheManager(Caffeine<Object, Object> caffeine) {
        CaffeineCacheManager cacheManager = new CaffeineCacheManager(NFE_CACHE);
        cacheManager.setCaffeine(caffeine);
        return cacheManager;
    }
}
