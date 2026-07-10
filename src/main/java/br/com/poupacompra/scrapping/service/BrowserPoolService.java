package br.com.poupacompra.scrapping.service;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;

import br.com.poupacompra.scrapping.config.ScrappingProperties;
import br.com.poupacompra.scrapping.exception.ScrapingException;
import br.com.poupacompra.scrapping.service.BrowserPoolService.BrowserInstance;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;

@Service
public class BrowserPoolService {
        private static final List<String> DEFAULT_BROWSER_ARGS = List.of(
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

    
    private static final Logger log = LoggerFactory.getLogger(BrowserPoolService.class);
    
    private final ScrappingProperties properties;
    private final BlockingQueue<BrowserInstance> browserPool;
    private final AtomicInteger browsersAvailable;
    private final List<String> browserArgs;
    
    private Playwright playwright;
    private volatile boolean initialized = false;
    
    public record BrowserInstance(
        int id,
        Browser browser,
        BrowserContext context,
        Page page
    ) {}
    
    public BrowserPoolService(ScrappingProperties properties) {
        this.properties = properties;
        this.browserPool = new LinkedBlockingQueue<>(properties.getBrowser().getPoolSize());
        this.browsersAvailable = new AtomicInteger(0);
        this.browserArgs = DEFAULT_BROWSER_ARGS;
    }

    @PostConstruct
    public void init() {
        log.info("Inicializando pool de {} browsers...", properties.getBrowser().getPoolSize());
        log.info(
            "Configuração do browser: headless={}, debug-ui-enabled={}, display={}",
            properties.getBrowser().isHeadless(),
            isDebugUiEnabled(),
            System.getenv("DISPLAY")
        );
        
        try {
            playwright = Playwright.create();

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
        boolean debugUiEnabled = isDebugUiEnabled();
        boolean headless = properties.getBrowser().isHeadless() || !debugUiEnabled;

        BrowserType.LaunchOptions launchOptions = new BrowserType.LaunchOptions()
            .setHeadless(headless)
                .setArgs(browserArgs);

        Path executablePath = resolveExecutablePath(
            properties.getBrowser().getExecutablePath(),
            System.getenv("PLAYWRIGHT_BROWSERS_PATH"),
            headless
        );
        if (executablePath != null) {
            launchOptions.setExecutablePath(executablePath);
            log.info("Usando executável do Chromium: {}", executablePath);
        }

        if (!headless) {
            log.info("Browser {} iniciado em modo visual para debug", id);
        }

        Browser browser = playwright.chromium().launch(launchOptions);
        
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

    private boolean isDebugUiEnabled() {
        return properties.getBrowser().isDebugUiEnabled()
            || Boolean.parseBoolean(System.getenv("SCRAPING_DEBUG_UI"));
    }
    
    static Path resolveExecutablePath(String configuredExecutablePath, String playwrightBrowsersPath, boolean headless) {
        if (StringUtils.hasText(configuredExecutablePath)) {
            Path configuredPath = Path.of(configuredExecutablePath);
            if (Files.isExecutable(configuredPath)) {
                return configuredPath;
            }

            log.warn(
                "Executable configurado não encontrado ou sem permissão de execução: {}. Tentando fallback automático.",
                configuredPath
            );
        }

        List<Path> candidatePaths = new ArrayList<>();

        if (StringUtils.hasText(playwrightBrowsersPath)) {
            candidatePaths.addAll(resolvePlaywrightCandidates(playwrightBrowsersPath, headless));
        }

        candidatePaths.add(Path.of("/usr/bin/chromium"));
        candidatePaths.add(Path.of("/usr/bin/chromium-browser"));
        candidatePaths.add(Path.of("/usr/bin/google-chrome"));
        candidatePaths.add(Path.of("/usr/bin/google-chrome-stable"));

        for (Path candidate : candidatePaths) {
            if (Files.isExecutable(candidate)) {
                return candidate;
            }
        }

        log.info("Nenhum executável de Chromium encontrado manualmente. Usando resolução padrão do Playwright.");
        return null;
    }

    private static List<Path> resolvePlaywrightCandidates(String playwrightBrowsersPath, boolean headless) {
        Path browsersHome = Path.of(playwrightBrowsersPath);
        if (!Files.isDirectory(browsersHome)) {
            return List.of();
        }

        List<String> relativeExecutables = headless
            ? List.of(
                "chrome-headless-shell-linux64/chrome-headless-shell",
                "chrome-linux64/chrome",
                "chrome-linux/chrome"
            )
            : List.of(
                "chrome-linux64/chrome",
                "chrome-linux/chrome"
            );

        try (Stream<Path> directories = Files.list(browsersHome)) {
            return directories
                .filter(Files::isDirectory)
                .filter(dir -> {
                    String name = dir.getFileName().toString();
                    return name.startsWith("chromium-") || name.startsWith("chromium_headless_shell-");
                })
                .sorted((left, right) -> right.getFileName().toString().compareTo(left.getFileName().toString()))
                .flatMap(dir -> relativeExecutables.stream().map(dir::resolve))
                .toList();
        } catch (Exception e) {
            log.warn("Falha ao listar executáveis em PLAYWRIGHT_BROWSERS_PATH={}: {}", playwrightBrowsersPath, e.getMessage());
            return List.of();
        }
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
                BrowserInstance recycledInstance = createBrowserInstance(instance.id(), browserArgs);
                closeInstance(instance);
                browserPool.offer(recycledInstance);
                browsersAvailable.incrementAndGet();
                log.debug("✓ Browser {} reciclado e retornado ao pool", instance.id());
            } catch (Exception e) {
                log.warn("⚠ Erro ao reciclar browser {}. Tentando retornar instância atual", instance.id(), e);
                try {
                    browserPool.offer(instance);
                    browsersAvailable.incrementAndGet();
                } catch (Exception fallbackError) {
                    log.error("✗ Falha ao retornar browser {} ao pool após erro de reciclagem", instance.id(), fallbackError);
                }
            }
        }
    }

    private void closeInstance(BrowserInstance instance) {
        try {
            instance.page().close();
        } catch (Exception e) {
            log.debug("Falha ao fechar page do browser {} durante reciclagem", instance.id(), e);
        }

        try {
            instance.context().close();
        } catch (Exception e) {
            log.debug("Falha ao fechar context do browser {} durante reciclagem", instance.id(), e);
        }

        try {
            instance.browser().close();
        } catch (Exception e) {
            log.debug("Falha ao fechar browser {} durante reciclagem", instance.id(), e);
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
