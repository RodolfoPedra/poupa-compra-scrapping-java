package br.com.poupacompra.scraping.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import br.com.poupacompra.scraping.dto.DadosNotaResponseDTO;

@FeignClient(name = "poupa-compra-api", path = "/integracao-poupa-compra")
public interface PoupaCompraApiClient {

    @PostMapping("/salvar-nota")
    void salvarNota(@RequestBody DadosNotaResponseDTO dadosNota);
}
