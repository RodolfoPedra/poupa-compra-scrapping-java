package br.com.poupacompra.scraping.service.strategy;

import java.util.Locale;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.TimeoutError;
import com.microsoft.playwright.options.WaitForSelectorState;

import br.com.poupacompra.scraping.config.ScrapingProperties;

public class SantaCatarinaPagePreparationStrategy extends PagePreparationStrategy {

    private static final Logger log = LoggerFactory.getLogger(SantaCatarinaPagePreparationStrategy.class);

    @Override
    public boolean prepare(Page page, String url, ScrapingProperties properties) {
        log.debug("Usando preparação específica para Santa Catarina: {}", url);

        try {
            if (isLocalMockEnabled(url)) {
                page.navigate("http://localhost:8181/mock/sc/validation", new Page.NavigateOptions()
                    .setTimeout(properties.getBrowser().getPageLoadTimeoutMs()));
            }

            page.waitForSelector(".cf-turnstile", new Page.WaitForSelectorOptions()
                .setTimeout(25000)
                .setState(WaitForSelectorState.ATTACHED));

            page.waitForSelector("#Body_Main_ButtonValidar", new Page.WaitForSelectorOptions()
                .setTimeout(25000)
                .setState(WaitForSelectorState.ATTACHED));

            page.locator("#Body_Main_ButtonValidar").first().click(new Locator.ClickOptions().setTimeout(25000));

            page.waitForSelector("#totalNota", new Page.WaitForSelectorOptions()
                .setTimeout(properties.getBrowser().getTimeoutMs())
                .setState(WaitForSelectorState.ATTACHED));

            page.waitForSelector("#totalNota", new Page.WaitForSelectorOptions()
                .setTimeout(25000)
                .setState(WaitForSelectorState.VISIBLE));

            return true;
        } catch (TimeoutError e) {
            log.warn("Falha na estratégia de validação SC: {}", e.getMessage());
            return false;
        } catch (Exception e) {
            log.warn("Erro inesperado na estratégia de validação SC: {}", e.getMessage());
            return false;
        }
    }

    private boolean isLocalMockEnabled(String url) {
        if (url == null) {
            return false;
        }
        String normalizedUrl = url.toLowerCase(Locale.ROOT);
        return normalizedUrl.contains("mock") || normalizedUrl.contains("localhost");
    }
}
