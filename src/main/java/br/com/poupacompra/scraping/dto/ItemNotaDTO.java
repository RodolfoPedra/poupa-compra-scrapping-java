package br.com.poupacompra.scraping.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record ItemNotaDTO(
    @JsonProperty("descricao")
    String descricao,
    
    @JsonProperty("quantidade")
    double quantidade,
    
    @JsonProperty("tipoUnidade")
    String tipoUnidade,
    
    @JsonProperty("valorUnitario")
    double valorUnitario,
    
    @JsonProperty("valorTotal")
    double valorTotal
) {
    public static Builder builder() {
        return new Builder();
    }
    
    public static class Builder {
        private String descricao = "";
        private double quantidade = 0;
        private String tipoUnidade = "";
        private double valorUnitario = 0;
        private double valorTotal = 0;
        
        public Builder descricao(String descricao) {
            this.descricao = descricao != null ? descricao : "";
            return this;
        }
        
        public Builder quantidade(double quantidade) {
            this.quantidade = quantidade;
            return this;
        }
        
        public Builder tipoUnidade(String tipoUnidade) {
            this.tipoUnidade = tipoUnidade != null ? tipoUnidade : "";
            return this;
        }
        
        public Builder valorUnitario(double valorUnitario) {
            this.valorUnitario = valorUnitario;
            return this;
        }
        
        public Builder valorTotal(double valorTotal) {
            this.valorTotal = valorTotal;
            return this;
        }
        
        public ItemNotaDTO build() {
            return new ItemNotaDTO(descricao, quantidade, tipoUnidade, valorUnitario, valorTotal);
        }
    }
}
