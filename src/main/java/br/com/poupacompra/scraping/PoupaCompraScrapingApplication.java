package br.com.poupacompra.scraping;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cloud.openfeign.EnableFeignClients;

@EnableCaching
@EnableFeignClients
@SpringBootApplication
public class PoupaCompraScrapingApplication {

    public static void main(String[] args) {
        System.out.println("Iniciando aplicação Poupa Compra Scraping");
        SpringApplication.run(PoupaCompraScrapingApplication.class, args);
    }
}
