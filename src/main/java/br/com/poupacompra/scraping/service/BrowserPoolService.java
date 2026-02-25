package br.com.poupacompra.scraping.service;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;

import br.com.poupacompra.scraping.config.ScrapingProperties;
import br.com.poupacompra.scraping.exception.ScrapingException;
import br.com.poupacompra.scraping.service.BrowserPoolService.BrowserInstance;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;

@Service
public class BrowserPoolService {
    
    private static final Logger log = LoggerFactory.getLogger(BrowserPoolService.class);
    
    private final ScrapingProperties properties;
    private final BlockingQueue<BrowserInstance> browserPool;
    private final AtomicInteger browsersAvailable;
    
    private Playwright playwright;
    private volatile boolean initialized = false;
    
    public record BrowserInstance(
        int id,
        Browser browser,
        BrowserContext context,
        Page page
    ) {}
    
    public BrowserPoolService(ScrapingProperties properties) {
        this.properties = properties;
        this.browserPool = new LinkedBlockingQueue<>(properties.getBrowser().getPoolSize());
        this.browsersAvailable = new AtomicInteger(0);
    }

    @PostConstruct
    public void init() {
        log.info("Inicializando pool de {} browsers...", properties.getBrowser().getPoolSize());
        
        try {
            playwright = Playwright.create();

            List<String> browserArgs = Arrays.asList(
                "--disable-blink-features=AutomationControlled",
                "--no-sandbox",
                "--disable-setuid-sandbox",
                "--disable-dev-shm-usage",
                "--disable-accelerated-2d-canvas",
                "--no-first-run",
                "--no-zygote",
                "--disable-gpu",
                "--disable-web-security",
                "--allow-running-insecure-content",
                "--ignore-certificate-errors",
                "--disable-features=IsolateOrigins,site-per-process",
                "--disable-background-timer-throttling",
                "--disable-backgrounding-occluded-windows",
                "--disable-renderer-backgrounding",
                "--disable-background-networking",
                "--disable-breakpad",
                "--disable-component-extensions-with-background-pages",
                "--disable-extensions",
                "--disable-features=TranslateUI,BlinkGenPropertyTrees",
                "--disable-ipc-flooding-protection",
                "--disable-hang-monitor",
                "--disable-popup-blocking",
                "--disable-prompt-on-repost",
                "--disable-sync",
                "--force-color-profile=srgb",
                "--metrics-recording-only",
                "--no-default-browser-check",
                "--password-store=basic",
                "--use-mock-keychain"
            );
            
            for (int i = 0; i < properties.getBrowser().getPoolSize(); i++) {
                BrowserInstance instance = createBrowserInstance(i, browserArgs);
                browserPool.offer(instance);
                browsersAvailable.incrementAndGet();
                log.info("Browser {}/{} criado", i + 1, properties.getBrowser().getPoolSize());
            }
            
            initialized = true;
            log.info("Pool de browsers inicializado com sucesso");

        } catch (Exception e) {
            log.error("Erro ao inicializar pool de browsers", e);
            throw new ScrapingException("Falha ao inicializar pool de browsers", e);
        }
    }
    
    private BrowserInstance createBrowserInstance(int id, List<String> browserArgs) {
        Browser browser = playwright.chromium().launch(
            new BrowserType.LaunchOptions()
                .setHeadless(properties.getBrowser().isHeadless())
                .setArgs(browserArgs)
        );
        
        BrowserContext context = browser.newContext(
            new Browser.NewContextOptions()
                .setViewportSize(800, 600)
                .setUserAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36")
                .setIgnoreHTTPSErrors(true)
                .setJavaScriptEnabled(true)
                .setBypassCSP(true)
        );
        
        context.route("**/*", route -> {
            String resourceType = route.request().resourceType();
            if (List.of("image", "stylesheet", "font", "media").contains(resourceType)) {
                route.abort();
            } else {
                route.resume();
            }
        });
        
        // Remove propriedades de automação (anti-detecção)
        context.addInitScript("""
            Object.defineProperty(navigator, 'webdriver', {
                get: () => undefined
            });
            
            window.chrome = {
                runtime: {}
            };
        """);
        
        Page page = context.newPage();
        page.setDefaultTimeout(properties.getBrowser().getTimeoutMs());
        
        return new BrowserInstance(id, browser, context, page);
    }
    
    /**
     * Obtém um browser do pool. Bloqueia até que um esteja disponível.
     */
    public BrowserInstance acquireBrowser(long timeoutSeconds) {
        if (!initialized) {
            throw new ScrapingException("Pool de browsers não inicializado");
        }
        
        try {
            BrowserInstance instance = browserPool.poll(timeoutSeconds, TimeUnit.SECONDS);
            if (instance == null) {
                throw new ScrapingException("Timeout ao aguardar browser disponível no pool");
            }
            browsersAvailable.decrementAndGet();
            log.debug("✓ Browser {} obtido do pool", instance.id());
            return instance;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new ScrapingException("Interrompido ao aguardar browser", e);
        }
    }
    
    /**
     * Retorna um browser ao pool após uso.
     */
    public void releaseBrowser(BrowserInstance instance) {
        if (instance != null) {
            try {
                browserPool.offer(instance);
                browsersAvailable.incrementAndGet();
                log.debug("✓ Browser {} retornado ao pool", instance.id());
            } catch (Exception e) {
                log.warn("⚠ Erro ao retornar browser {} ao pool", instance.id(), e);
            }
        }
    }
    
    public int getAvailableCount() {
        return browsersAvailable.get();
    }
    
    public int getPoolSize() {
        return properties.getBrowser().getPoolSize();
    }
    
    @PreDestroy
    public void cleanup() {
        log.info("Encerrando aplicação...");
        log.info("Fechando {} browsers...", properties.getBrowser().getPoolSize());
        
        while (!browserPool.isEmpty()) {
            try {
                BrowserInstance instance = browserPool.poll();
                if (instance != null) {
                    instance.page().close();
                    instance.context().close();
                    instance.browser().close();
                    log.info("Browser {} fechado", instance.id());
                }
            } catch (Exception e) {
                log.warn("Erro ao fechar browser", e);
            }
        }
        
        if (playwright != null) {
            playwright.close();
            log.info("Playwright encerrado");
        }

        log.info("Cleanup completo");
    }
}
