package br.com.poupacompra.scrapping.service.strategy;

import com.microsoft.playwright.Page;

import br.com.poupacompra.scrapping.config.ScrappingProperties;

public abstract class PageNavigateStrategy {

  public abstract void navigate(Page page, String url, ScrappingProperties properties);
}
