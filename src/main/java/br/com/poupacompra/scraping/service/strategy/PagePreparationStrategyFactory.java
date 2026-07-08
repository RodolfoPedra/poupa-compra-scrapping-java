package br.com.poupacompra.scraping.service.strategy;

import java.util.Locale;

import org.springframework.stereotype.Component;

import br.com.poupacompra.scraping.config.ScrapingProperties;

@Component
public class PagePreparationStrategyFactory {

    private final ScrapingProperties properties;

    public PagePreparationStrategyFactory(ScrapingProperties properties) {
        this.properties = properties;
    }

    public PagePreparationStrategy getStrategy(String url) {
        if (url == null) {
            return new DefaultPagePreparationStrategy();
        }

        String normalizedUrl = url.toLowerCase(Locale.ROOT);
        if (normalizedUrl.contains(".sc.gov.br") || normalizedUrl.contains("sef.sc.gov.br")
                || normalizedUrl.contains("/sc/")) {
            return new SantaCatarinaPagePreparationStrategy();
        }

        return new DefaultPagePreparationStrategy();
    }
}
