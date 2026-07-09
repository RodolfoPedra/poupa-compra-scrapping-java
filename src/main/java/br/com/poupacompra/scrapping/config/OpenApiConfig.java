package br.com.poupacompra.scrapping.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;

import java.util.List;

@Configuration
public class OpenApiConfig {
    
    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
            .info(new Info()
                .title("Poupa Compra Scraping API")
                .description("""
                    API de scraping de Notas Fiscais de Consumidor Eletrônicas (NFCe).
                    
                    Este serviço extrai dados estruturados de notas fiscais eletrônicas a partir
                    de URLs fornecidas, utilizando técnicas de web scraping com Playwright/Chromium.
                    
                    **Funcionalidades:**
                    - Extração de dados do estabelecimento (nome, CNPJ, endereço)
                    - Extração dos itens da nota (produtos, quantidades, valores)
                    - Extração de informações gerais da nota (chave de acesso, data, valor total)
                    - Cache de resultados para otimização de desempenho
                    """)
                .version("1.0.0")
                .contact(new Contact()
                    .name("Poupa Compra")
                    .email("contato@poupacompra.com.br"))
                .license(new License()
                    .name("Proprietary")
                    .url("https://poupacompra.com.br")))
            .servers(List.of(
                new Server()
                    .url("http://localhost:8181")
                    .description("Servidor de desenvolvimento local"),
                new Server()
                    .url("https://api.poupacompra.com.br")
                    .description("Servidor de produção")
            ));
    }
}
