package br.com.poupacompra.scraping.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record NotaDTO(
    @JsonProperty("quantidadeItens")
    int quantidadeItens,
    
    @JsonProperty("valorTotal")
    double valorTotal,
    
    @JsonProperty("usuario")
    int usuario,
    
    @JsonProperty("ufCfe")
    String ufCfe,
    
    @JsonProperty("urlCfe")
    String urlCfe,
    
    @JsonProperty("chaveAcesso")
    String chaveAcesso
) {
    public NotaDTO() {
        this(0, 0, 0, "", "", "");
    }

    public static Builder builder() {
        return new Builder();
    }
    
    public static class Builder {
        private int quantidadeItens = 0;
        private double valorTotal = 0;
        private int usuario = 0;
        private String ufCfe = "";
        private String urlCfe = "";
        private String chaveAcesso = "";
        
        public Builder quantidadeItens(int quantidadeItens) {
            this.quantidadeItens = quantidadeItens;
            return this;
        }
        
        public Builder valorTotal(double valorTotal) {
            this.valorTotal = valorTotal;
            return this;
        }
        
        public Builder usuario(int usuario) {
            this.usuario = usuario;
            return this;
        }
        
        public Builder ufCfe(String ufCfe) {
            this.ufCfe = ufCfe != null ? ufCfe : "";
            return this;
        }
        
        public Builder urlCfe(String urlCfe) {
            this.urlCfe = urlCfe != null ? urlCfe : "";
            return this;
        }
        
        public Builder chaveAcesso(String chaveAcesso) {
            this.chaveAcesso = chaveAcesso != null ? chaveAcesso : "";
            return this;
        }
        
        public NotaDTO build() {
            return new NotaDTO(quantidadeItens, valorTotal, usuario, ufCfe, urlCfe, chaveAcesso);
        }
    }
}
