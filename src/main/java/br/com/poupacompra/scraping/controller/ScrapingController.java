package br.com.poupacompra.scraping.controller;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import br.com.poupacompra.scraping.dto.DadosNotaResponseDTO;
import br.com.poupacompra.scraping.service.BrowserPoolService;
import br.com.poupacompra.scraping.service.CacheService;
import br.com.poupacompra.scraping.service.NfeScrapingService;

/**
 * Controller para os endpoints de scraping de NFCe.
 * 
 * Endpoints:
 * - POST /dados-nota?url=<link> - Realiza scraping da NFCe
 * - POST /cache/clear - Limpa o cache
 * - GET /cache/stats - Estatísticas do cache
 */
@RestController
public class ScrapingController {
    
    private static final Logger log = LoggerFactory.getLogger(ScrapingController.class);
    
    private final NfeScrapingService nfeScrapingService;
    private final CacheService cacheService;
    private final BrowserPoolService browserPoolService;
    
    public ScrapingController(
            NfeScrapingService nfeScrapingService,
            CacheService cacheService,
            BrowserPoolService browserPoolService) {
        this.nfeScrapingService = nfeScrapingService;
        this.cacheService = cacheService;
        this.browserPoolService = browserPoolService;
    }
    
    /**
     * Endpoint principal para scraping de NFe.
     * 
     * @param url URL da NFe a ser processada
     * @return Dados extraídos da nota fiscal
     */
    @PostMapping("/dados-nota")
    public ResponseEntity<DadosNotaResponseDTO> getDadosNota(@RequestParam("url") String url) {
        if (url == null || url.isBlank()) {
            throw new IllegalArgumentException("URL não fornecida");
        }
        
        long startTime = System.currentTimeMillis();
        log.info("\n" + "=".repeat(60));
        log.info("Nova requisição recebida");
        
        DadosNotaResponseDTO result = nfeScrapingService.scrapeNfe(url);
        
        long elapsed = System.currentTimeMillis() - startTime;
        log.info("✓ Request finalizada em {}ms", elapsed);
        log.info("=".repeat(60) + "\n");
        
        return ResponseEntity.ok(result);
    }
    
    /**
     * Limpa o cache manualmente.
     */
    @PostMapping("/cache/clear")
    public ResponseEntity<Map<String, Object>> clearCache() {
        long removed = cacheService.clearCache();
        return ResponseEntity.ok(Map.of(
            "message", removed + " entradas removidas do cache"
        ));
    }
    
    /**
     * Retorna estatísticas detalhadas do cache.
     */
    @GetMapping("/cache/stats")
    public ResponseEntity<Map<String, Object>> cacheStats() {
        Map<String, Object> stats = cacheService.getCacheStats();
        
        // Adiciona informações do browser pool
        return ResponseEntity.ok(Map.of(
            "cache", stats,
            "browserPool", Map.of(
                "size", browserPoolService.getPoolSize(),
                "available", browserPoolService.getAvailableCount()
            )
        ));
    }
}
