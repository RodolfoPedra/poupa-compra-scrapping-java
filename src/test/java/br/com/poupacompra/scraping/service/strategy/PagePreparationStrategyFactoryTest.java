package br.com.poupacompra.scraping.service.strategy;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

import br.com.poupacompra.scraping.config.ScrapingProperties;
import br.com.poupacompra.scraping.service.NfeScrapingService;

class PagePreparationStrategyFactoryTest {

    @Test
    void shouldUseSantaCatarinaStrategyForScUrls() {
        PagePreparationStrategyFactory factory = new PagePreparationStrategyFactory(new ScrapingProperties());

        PagePreparationStrategy strategy = factory.getStrategy("https://www.sef.sc.gov.br/consulta/nfce");

        assertThat(strategy).isInstanceOf(SantaCatarinaPagePreparationStrategy.class);
    }

    @Test
    void shouldUseDefaultStrategyForOtherStates() {
        PagePreparationStrategyFactory factory = new PagePreparationStrategyFactory(new ScrapingProperties());

        PagePreparationStrategy strategy = factory.getStrategy("https://www.sefaz.sp.gov.br/consulta/nfce");

        assertThat(strategy).isInstanceOf(DefaultPagePreparationStrategy.class);
    }

    @Test
    void shouldUseCurrentUrlWhenNavigationChangesAfterPreparation() {
        String effectiveUrl = NfeScrapingService.resolveEffectiveUrl(
            "https://sat.sef.sc.gov.br/nfce/consulta?p=123",
            "https://sat.sef.sc.gov.br/tax.NET/Sat.DFe.NFCe.Web/Consultas/NFCe_Detalhes.aspx?rq=abc"
        );

        assertThat(effectiveUrl).isEqualTo("https://sat.sef.sc.gov.br/tax.NET/Sat.DFe.NFCe.Web/Consultas/NFCe_Detalhes.aspx?rq=abc");
    }
}
