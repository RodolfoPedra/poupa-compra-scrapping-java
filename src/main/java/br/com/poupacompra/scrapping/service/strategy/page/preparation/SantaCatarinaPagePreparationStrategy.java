package br.com.poupacompra.scrapping.service.strategy.page.preparation;

import java.util.Locale;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.TimeoutError;
import com.microsoft.playwright.options.LoadState;
import com.microsoft.playwright.options.WaitForSelectorState;

import br.com.poupacompra.scrapping.config.ScrappingProperties;
import br.com.poupacompra.scrapping.service.strategy.PagePreparationStrategy;

public class SantaCatarinaPagePreparationStrategy extends PagePreparationStrategy {

    private static final Logger log = LoggerFactory.getLogger(SantaCatarinaPagePreparationStrategy.class);

    @Override
    public boolean prepare(Page page, String url, ScrappingProperties properties) {
        log.debug("Usando preparação específica para Santa Catarina: {}", url);

        try {
            if (isLocalMockEnabled(url)) {
                page.navigate("http://localhost:8181/mock/sc/validation", new Page.NavigateOptions()
                    .setTimeout(properties.getBrowser().getPageLoadTimeoutMs()));
            }

            if (isNotaPageLoaded(page, 2000)) {
                return true;
            }

            page.waitForSelector("#Body_Main_ButtonValidar", new Page.WaitForSelectorOptions()
                .setTimeout(25000)
                .setState(WaitForSelectorState.ATTACHED));

            waitForTurnstileToken(page, 35000);

            page.locator("#Body_Main_ButtonValidar").first().click(new Locator.ClickOptions().setTimeout(25000));
            page.waitForLoadState(LoadState.DOMCONTENTLOADED, new Page.WaitForLoadStateOptions().setTimeout(25000));

            log.info("URL após clicar no botão de validação: {}", page.url());

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

    private boolean isNotaPageLoaded(Page page, int timeoutMs) {
        try {
            page.waitForSelector("#totalNota", new Page.WaitForSelectorOptions()
                .setTimeout(timeoutMs)
                .setState(WaitForSelectorState.ATTACHED));
            return true;
        } catch (TimeoutError e) {
            return false;
        }
    }

    private void waitForTurnstileToken(Page page, int timeoutMs) {
        page.waitForSelector(".cf-turnstile", new Page.WaitForSelectorOptions()
            .setTimeout(timeoutMs)
            .setState(WaitForSelectorState.ATTACHED));

        // Aguarda o token do Turnstile antes do postback de validação.
        page.waitForFunction(
            """
                () => {
                    const tokenField = document.querySelector("input[name='cf-turnstile-response']");
                    if (!tokenField) {
                        return false;
                    }
                    const token = (tokenField.value || '').trim();
                    return token.length > 20;
                }
            """,
            null,
            new Page.WaitForFunctionOptions().setTimeout(timeoutMs)
        );
    }
}
