package br.com.poupacompra.scrapping.controller;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import br.com.poupacompra.scrapping.dto.DadosNotaResponseDTO;
import br.com.poupacompra.scrapping.service.BrowserPoolService;
import br.com.poupacompra.scrapping.service.CacheService;
import br.com.poupacompra.scrapping.service.NfeScrapingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * Controller para os endpoints de scraping de NFCe.
 * 
 * Endpoints:
 * - POST /dados-nota?url=<link> - Realiza scraping da NFCe
 * - GET /cache/clear - Limpa o cache
 * - GET /cache/stats - Estatísticas do cache
 */
@RestController
@Tag(name = "Scraping NFCe", description = "API para scraping de Notas Fiscais de Consumidor Eletrônicas (NFCe)")
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
     * @param idUsuario ID do usuário que está realizando a requisição
     * @return Dados extraídos da nota fiscal
     */
    @Operation(
        summary = "Extrair dados de NFCe",
        description = "Realiza scraping de uma Nota Fiscal de Consumidor Eletrônica (NFCe) a partir da URL fornecida e retorna os dados estruturados"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Dados da nota fiscal extraídos com sucesso",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = DadosNotaResponseDTO.class))
        ),
        @ApiResponse(
            responseCode = "400",
            description = "URL inválida ou não fornecida",
            content = @Content(mediaType = "application/json")
        ),
        @ApiResponse(
            responseCode = "500",
            description = "Erro ao realizar scraping da nota fiscal",
            content = @Content(mediaType = "application/json")
        )
    })
    @PostMapping("/dados-nota")
    public ResponseEntity<DadosNotaResponseDTO> getDadosNota(
            @Parameter(description = "URL completa da NFCe a ser processada", required = true, example = "https://nfce.fazenda.sp.gov.br/...")
            @RequestParam("urlNota") String url,
            @Parameter(description = "ID do usuário que está realizando a requisição", required = true, example = "12345")
            @RequestParam(name = "idUsuario") Long idUsuario) {
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
        
        return ResponseEntity.ok().body(result);
    }
    
    /**
     * Limpa o cache manualmente.
     */
    @Operation(
        summary = "Limpar cache",
        description = "Remove todas as entradas do cache de notas fiscais"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Cache limpo com sucesso",
            content = @Content(mediaType = "application/json")
        )
    })
    @GetMapping("/cache/clear")
    public ResponseEntity<Map<String, Object>> clearCache() {
        long removed = cacheService.clearCache();
        return ResponseEntity.ok(Map.of(
            "message", removed + " entradas removidas do cache"
        ));
    }
    
    /**
     * Retorna estatísticas detalhadas do cache.
     */
    @Operation(
        summary = "Estatísticas do cache",
        description = "Retorna estatísticas detalhadas do cache e informações sobre o pool de browsers"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Estatísticas retornadas com sucesso",
            content = @Content(mediaType = "application/json")
        )
    })
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
