package br.com.poupacompra.scrapping.service.strategy;

import java.util.Locale;

import org.springframework.stereotype.Component;

import br.com.poupacompra.scrapping.config.ScrappingProperties;
import br.com.poupacompra.scrapping.service.strategy.page.preparation.DefaultPagePreparationStrategy;
import br.com.poupacompra.scrapping.service.strategy.page.preparation.SantaCatarinaPagePreparationStrategy;

@Component
public class PagePreparationStrategyFactory {

    private final ScrappingProperties properties;

    public PagePreparationStrategyFactory(ScrappingProperties properties) {
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
