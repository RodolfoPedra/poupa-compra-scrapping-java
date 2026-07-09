package br.com.poupacompra.scrapping.service.strategy;

import com.microsoft.playwright.Page;

import br.com.poupacompra.scrapping.config.ScrappingProperties;

public abstract class PagePreparationStrategy {

    public abstract boolean prepare(Page page, String url, ScrappingProperties properties);
}
