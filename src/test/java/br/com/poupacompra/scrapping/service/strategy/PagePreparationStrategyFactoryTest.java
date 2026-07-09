package br.com.poupacompra.scrapping.service.strategy;

import org.junit.jupiter.api.Test;

import br.com.poupacompra.scrapping.config.ScrappingProperties;
import br.com.poupacompra.scrapping.service.NfeScrapingService;
import br.com.poupacompra.scrapping.service.strategy.PagePreparationStrategy;
import br.com.poupacompra.scrapping.service.strategy.PagePreparationStrategyFactory;
import br.com.poupacompra.scrapping.service.strategy.page.preparation.DefaultPagePreparationStrategy;
import br.com.poupacompra.scrapping.service.strategy.page.preparation.SantaCatarinaPagePreparationStrategy;

import static org.assertj.core.api.Assertions.assertThat;

class PagePreparationStrategyFactoryTest {

    @Test
    void shouldUseSantaCatarinaStrategyForScUrls() {
        PagePreparationStrategyFactory factory = new PagePreparationStrategyFactory(new ScrappingProperties());

        PagePreparationStrategy strategy = factory.getStrategy("https://www.sef.sc.gov.br/consulta/nfce");

        assertThat(strategy).isInstanceOf(SantaCatarinaPagePreparationStrategy.class);
    }

    @Test
    void shouldUseDefaultStrategyForOtherStates() {
        PagePreparationStrategyFactory factory = new PagePreparationStrategyFactory(new ScrappingProperties());

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
