package br.com.poupacompra.scraping.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record EstabelecimentoDTO(
    @JsonProperty("nomeEstabelecimento")
    String nomeEstabelecimento,
    
    @JsonProperty("cpfCnpj")
    String cpfCnpj,
    
    @JsonProperty("endereco")
    String endereco
) {
    public EstabelecimentoDTO() {
        this("", "", "");
    }
    
    public static Builder builder() {
        return new Builder();
    }
    
    public static class Builder {
        private String nomeEstabelecimento = "";
        private String cpfCnpj = "";
        private String endereco = "";
        
        public Builder nomeEstabelecimento(String nomeEstabelecimento) {
            this.nomeEstabelecimento = nomeEstabelecimento != null ? nomeEstabelecimento : "";
            return this;
        }
        
        public Builder cpfCnpj(String cpfCnpj) {
            this.cpfCnpj = cpfCnpj != null ? cpfCnpj : "";
            return this;
        }
        
        public Builder endereco(String endereco) {
            this.endereco = endereco != null ? endereco : "";
            return this;
        }
        
        public EstabelecimentoDTO build() {
            return new EstabelecimentoDTO(nomeEstabelecimento, cpfCnpj, endereco);
        }
    }
}
