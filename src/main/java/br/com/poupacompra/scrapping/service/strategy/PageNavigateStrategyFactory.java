package br.com.poupacompra.scrapping.service.strategy;

import org.springframework.stereotype.Component;

import br.com.poupacompra.scrapping.config.ScrappingProperties;
import br.com.poupacompra.scrapping.service.strategy.page.navigate.DefaultNavigateStrategy;
import br.com.poupacompra.scrapping.service.strategy.page.navigate.ScNavigateStrategy;

@Component
public class PageNavigateStrategyFactory {

  private final ScrappingProperties properties;

  public PageNavigateStrategyFactory(ScrappingProperties properties) {
    this.properties = properties;
  }

  public PageNavigateStrategy getStrategy(String url) {
    if (url == null) {
      return new DefaultNavigateStrategy();
    }

    String normalizedUrl = url.toLowerCase();
    if (normalizedUrl.contains(".sc.gov.br") || normalizedUrl.contains("sef.sc.gov.br")
        || normalizedUrl.contains("/sc/")) {
      return new ScNavigateStrategy();
    }

    return new DefaultNavigateStrategy();
  }
}
