package br.com.poupacompra.scrapping.service.strategy.page.navigate;

import java.util.Locale;

import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.WaitUntilState;

import br.com.poupacompra.scrapping.config.ScrappingProperties;
import br.com.poupacompra.scrapping.service.strategy.PageNavigateStrategy;

public class ScNavigateStrategy extends PageNavigateStrategy {

  @Override
  public void navigate(Page page, String url, ScrappingProperties properties) {

    try {

      if (isLocalMockEnabled(url)) {
        page.navigate("http://localhost:8181/mock/sc/validation", new Page.NavigateOptions()
            .setTimeout(properties.getBrowser().getPageLoadTimeoutMs()));
      } else {
        page.navigate("https://sat.sef.sc.gov.br/nfce/consulta", new Page.NavigateOptions()
            .setTimeout(properties.getBrowser().getPageLoadTimeoutMs())
            .setWaitUntil(WaitUntilState.COMMIT));
      }



    } catch (Exception e) {
      // TODO: handle exception
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
