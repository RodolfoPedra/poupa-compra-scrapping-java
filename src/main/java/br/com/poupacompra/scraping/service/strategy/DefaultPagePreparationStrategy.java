package br.com.poupacompra.scraping.service.strategy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.microsoft.playwright.Page;
import com.microsoft.playwright.TimeoutError;
import com.microsoft.playwright.options.WaitForSelectorState;

import br.com.poupacompra.scraping.config.ScrapingProperties;

public class DefaultPagePreparationStrategy extends PagePreparationStrategy {

    private static final Logger log = LoggerFactory.getLogger(DefaultPagePreparationStrategy.class);

    @Override
    public boolean prepare(Page page, String url, ScrapingProperties properties) {
        log.debug("Usando preparação padrão para: {}", url);

        try {
            page.waitForSelector("#totalNota", new Page.WaitForSelectorOptions()
                .setTimeout(properties.getBrowser().getTimeoutMs())
                .setState(WaitForSelectorState.ATTACHED));

            page.waitForSelector("#totalNota", new Page.WaitForSelectorOptions()
                .setTimeout(3000)
                .setState(WaitForSelectorState.VISIBLE));

            return true;
        } catch (TimeoutError e) {
            log.warn("Conteúdo não encontrado com estratégia padrão: {}", e.getMessage());
            return false;
        }
    }
}
