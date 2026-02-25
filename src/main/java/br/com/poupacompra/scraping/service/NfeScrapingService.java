package br.com.poupacompra.scraping.service;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.TimeoutError;
import com.microsoft.playwright.options.WaitForSelectorState;
import com.microsoft.playwright.options.WaitUntilState;

import br.com.poupacompra.scraping.config.CacheConfig;
import br.com.poupacompra.scraping.config.ScrapingProperties;
import br.com.poupacompra.scraping.dto.DadosNotaResponseDTO;
import br.com.poupacompra.scraping.dto.EstabelecimentoDTO;
import br.com.poupacompra.scraping.dto.ItemNotaDTO;
import br.com.poupacompra.scraping.dto.NotaDTO;
import br.com.poupacompra.scraping.exception.ScrapingException;
import br.com.poupacompra.scraping.service.BrowserPoolService.BrowserInstance;

@Service
public class NfeScrapingService {
    
    private static final Logger log = LoggerFactory.getLogger(NfeScrapingService.class);

    private static final Pattern REGEX_UF = Pattern.compile("\\.([a-z]{2})\\.gov\\.br");
    
    private final BrowserPoolService browserPoolService;
    private final ScrapingProperties properties;
    
    public NfeScrapingService(BrowserPoolService browserPoolService, ScrapingProperties properties) {
        this.browserPoolService = browserPoolService;
        this.properties = properties;
    }
    
    /**
     * @param url URL da NFe
     * @return Dados extraídos da nota fiscal
     */
    @Cacheable(value = CacheConfig.NFE_CACHE, key = "#root.target.getCacheKey(#url)")
    public DadosNotaResponseDTO scrapeNfe(String url) {
        log.info("→ Iniciando scraping para: {}...", truncateUrl(url, 60));
        long startTime = System.currentTimeMillis();
        
        BrowserInstance browserInstance = null;
        
        try {
            browserInstance = browserPoolService.acquireBrowser(10);
            log.info("✓ Browser {} obtido do pool", browserInstance.id());
            
            Page page = browserInstance.page();
            
            DadosNotaResponseDTO result = performScraping(page, url);
            
            long elapsed = System.currentTimeMillis() - startTime;
            log.info("✓ Scraping completo em {}ms", elapsed);
            
            return result;
            
        } catch (Exception e) {
            log.error("✗ Erro ao realizar scraping: {}", e.getMessage(), e);
            throw new ScrapingException("Erro ao realizar scraping: " + e.getMessage(), url, e);
            
        } finally {
            if (browserInstance != null) {
                browserPoolService.releaseBrowser(browserInstance);
                log.debug("Browser {} retornado ao pool", browserInstance.id());
            }
        }
    }
    
    public String getCacheKey(String url) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] digest = md.digest(url.getBytes());
            StringBuilder sb = new StringBuilder();
            for (byte b : digest) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            return String.valueOf(url.hashCode());
        }
    }
    
    private DadosNotaResponseDTO performScraping(Page page, String url) {
        log.debug("Acessando: {}...", truncateUrl(url, 60));
        
        try {
            page.navigate(url, new Page.NavigateOptions()
                .setTimeout(properties.getBrowser().getPageLoadTimeoutMs())
                .setWaitUntil(WaitUntilState.COMMIT));
            
            log.debug("Página carregada, URL atual: {}...", truncateUrl(page.url(), 70));
            
            // Aguarda reCAPTCHA e conteúdo
            if (!waitForContent(page)) {
                throw new ScrapingException("Página não carregou - possível bloqueio por reCAPTCHA ou timeout", url);
            }
            
        } catch (TimeoutError e) {
            log.error("Timeout ao acessar URL: {}", e.getMessage());
            throw new ScrapingException("Timeout ao acessar página", url, e);
        } catch (ScrapingException e) {
            throw e;
        } catch (Exception e) {
            log.error("Erro ao acessar URL: {}", e.getMessage());
            throw new ScrapingException("Erro ao acessar página", url, e);
        }
        
        // Extrai dados
        return extractData(page, url);
    }
    
    private boolean waitForContent(Page page) {
        long startTime = System.currentTimeMillis();
        log.debug("  → Aguardando conteúdo carregar...");
        
        try {
            page.waitForSelector("#totalNota", new Page.WaitForSelectorOptions()
                .setTimeout(properties.getBrowser().getTimeoutMs())
                .setState(WaitForSelectorState.ATTACHED));
            
            long elapsed = System.currentTimeMillis() - startTime;
            log.debug("Elemento 'totalNota' encontrado em {}ms", elapsed);
            
            page.waitForSelector("#totalNota", new Page.WaitForSelectorOptions()
                .setTimeout(3000)
                .setState(WaitForSelectorState.VISIBLE));
            
            log.debug("Página totalmente carregada em {}ms", System.currentTimeMillis() - startTime);
            return true;
            
        } catch (TimeoutError e) {
            long elapsed = System.currentTimeMillis() - startTime;
            log.warn("Timeout após {}ms", elapsed);
            log.warn("URL atual: {}", page.url());
            
            try {
                String bodyText = page.locator("body").textContent(new Locator.TextContentOptions().setTimeout(2000));
                log.warn("Conteúdo da página: {}", bodyText.substring(0, Math.min(300, bodyText.length())));
            } catch (Exception ex) {
                log.warn("Não foi possível ler conteúdo da página");
            }
            
            return false;
        } catch (Exception e) {
            log.error("✗ Erro inesperado: {}", e.getMessage());
            return false;
        }
    }
    
    @SuppressWarnings("unchecked")
    private DadosNotaResponseDTO extractData(Page page, String url) {
        Matcher ufMatcher = REGEX_UF.matcher(url);
        String ufCfe = ufMatcher.find() ? ufMatcher.group(1).toUpperCase() : "";
        
        Map<String, Object> dadosPrincipais = (Map<String, Object>) page.evaluate("""
            () => {
                const totalNota = document.querySelector('#totalNota');
                const infos = document.querySelector('#infos');
                const infoEstab = document.querySelector('.txtCenter');
                
                const linhasTotal = totalNota?.querySelectorAll('#linhaTotal');
                const textElements = infoEstab?.querySelectorAll('.text');
                
                return {
                    qtdItens: linhasTotal?.[0]?.querySelector('span')?.textContent || '',
                    valorTotal: linhasTotal?.[1]?.querySelector('span')?.textContent || '',
                    chaveAcesso: infos?.querySelector('.chave')?.textContent || '',
                    nomeEstab: infoEstab?.querySelector('#u20')?.textContent || '',
                    cpfCnpj: textElements?.[0]?.textContent || '',
                    endereco: textElements?.[1]?.textContent || ''
                };
            }
        """);
        
        String cpfCnpjRaw = extractAfterColon((String) dadosPrincipais.get("cpfCnpj"));
        String cpfCnpj = cpfCnpjRaw.replaceAll("[.\\-/]", "");
        
        EstabelecimentoDTO estabelecimento = EstabelecimentoDTO.builder()
            .nomeEstabelecimento((String) dadosPrincipais.get("nomeEstab"))
            .cpfCnpj(cpfCnpj)
            .endereco((String) dadosPrincipais.get("endereco"))
            .build();
        
        List<Map<String, String>> produtosData = (List<Map<String, String>>) page.evaluate("""
            () => {
                const produtos = [];
                const rows = document.querySelectorAll("tr[id^='Item +']");
                
                rows.forEach(row => {
                    const colunas = row.querySelectorAll('td');
                    if (colunas.length >= 2) {
                        const col0 = colunas[0];
                        produtos.push({
                            nome: col0.querySelector('.txtTit')?.textContent || '',
                            qtd: col0.querySelector('.Rqtd')?.textContent || '',
                            un: col0.querySelector('.RUN')?.textContent || '',
                            vlUnit: col0.querySelector('.RvlUnit')?.textContent || '',
                            vlTotal: colunas[1].querySelector('span')?.textContent || ''
                        });
                    }
                });
                
                return produtos;
            }
        """);
        
        List<ItemNotaDTO> itensNota = new ArrayList<>();
        for (Map<String, String> prod : produtosData) {
            try {
                ItemNotaDTO item = ItemNotaDTO.builder()
                    .descricao(prod.get("nome"))
                    .quantidade(extractNumeric(extractAfterColon(prod.get("qtd"))))
                    .tipoUnidade(extractAfterColon(prod.get("un")))
                    .valorUnitario(extractNumeric(extractAfterColon(prod.get("vlUnit"))))
                    .valorTotal(extractNumeric(prod.get("vlTotal")))
                    .build();
                itensNota.add(item);
            } catch (Exception e) {
                log.warn("  ⚠ Erro ao processar produto: {}", e.getMessage());
            }
        }
        
        NotaDTO nota = NotaDTO.builder()
            .quantidadeItens((int) extractNumeric((String) dadosPrincipais.get("qtdItens")))
            .valorTotal(extractNumeric((String) dadosPrincipais.get("valorTotal")))
            .usuario(3)
            .ufCfe(ufCfe)
            .urlCfe(url + " via scrapping docker")
            .chaveAcesso(dadosPrincipais.get("chaveAcesso") + " via scrapping docker")
            .build();

        return DadosNotaResponseDTO.builder()
            .estabelecimento(estabelecimento)
            .itensNota(itensNota)
            .nota(nota)
            .build();
    }
    
    private double extractNumeric(String text) {
        if (text == null || text.isBlank()) {
            return 0;
        }
        String cleaned = text.replaceAll("[^\\d.,]", "").replace(",", ".");
        try {
            return Double.parseDouble(cleaned);
        } catch (NumberFormatException e) {
            return 0;
        }
    }
    
    private String extractAfterColon(String text) {
        if (text == null) return "";
        int colonIndex = text.lastIndexOf(':');
        if (colonIndex >= 0 && colonIndex < text.length() - 1) {
            return text.substring(colonIndex + 1).trim();
        }
        return text.trim();
    }
    
    private String truncateUrl(String url, int maxLength) {
        if (url == null) return "";
        return url.length() > maxLength ? url.substring(0, maxLength) + "..." : url;
    }
}
