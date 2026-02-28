package br.com.poupacompra.scraping.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Propriedades de configuração do application.yml
 */
@Component
@ConfigurationProperties(prefix = "scraping")
public class ScrapingProperties {
    
    private Browser browser = new Browser();
    private Cache cache = new Cache();
    
    public Browser getBrowser() {
        return browser;
    }
    
    public void setBrowser(Browser browser) {
        this.browser = browser;
    }
    
    public Cache getCache() {
        return cache;
    }
    
    public void setCache(Cache cache) {
        this.cache = cache;
    }
    
    public static class Browser {
        private int poolSize = 3;
        private boolean headless = true;
        private int timeoutMs = 25000;
        private int pageLoadTimeoutMs = 30000;
        
        public int getPoolSize() {
            return poolSize;
        }
        
        public void setPoolSize(int poolSize) {
            this.poolSize = poolSize;
        }
        
        public boolean isHeadless() {
            return headless;
        }
        
        public void setHeadless(boolean headless) {
            this.headless = headless;
        }
        
        public int getTimeoutMs() {
            return timeoutMs;
        }
        
        public void setTimeoutMs(int timeoutMs) {
            this.timeoutMs = timeoutMs;
        }
        
        public int getPageLoadTimeoutMs() {
            return pageLoadTimeoutMs;
        }
        
        public void setPageLoadTimeoutMs(int pageLoadTimeoutMs) {
            this.pageLoadTimeoutMs = pageLoadTimeoutMs;
        }
    }
    
    public static class Cache {
        private int ttlHours = 24;
        private int maxSize = 1000;
        
        public int getTtlHours() {
            return ttlHours;
        }
        
        public void setTtlHours(int ttlHours) {
            this.ttlHours = ttlHours;
        }
        
        public int getMaxSize() {
            return maxSize;
        }
        
        public void setMaxSize(int maxSize) {
            this.maxSize = maxSize;
        }
    }
}
