package br.com.poupacompra.scrapping.service.strategy.page.navigate;

import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.WaitUntilState;

import br.com.poupacompra.scrapping.config.ScrappingProperties;
import br.com.poupacompra.scrapping.service.strategy.PageNavigateStrategy;

public class DefaultNavigateStrategy extends PageNavigateStrategy {

  @Override
  public void navigate(Page page, String url, ScrappingProperties properties) {
            page.navigate(url, new Page.NavigateOptions()
                .setTimeout(properties.getBrowser().getPageLoadTimeoutMs())
                .setWaitUntil(WaitUntilState.COMMIT));
  }

}
