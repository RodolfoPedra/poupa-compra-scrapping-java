package br.com.poupacompra.scraping.service.strategy;

import com.microsoft.playwright.Page;

import br.com.poupacompra.scraping.config.ScrapingProperties;

public abstract class PagePreparationStrategy {

    public abstract boolean prepare(Page page, String url, ScrapingProperties properties);
}
