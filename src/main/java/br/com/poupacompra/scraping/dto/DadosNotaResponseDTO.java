package br.com.poupacompra.scraping.dto;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

public record DadosNotaResponseDTO(
    @JsonProperty("estabelecimento")
    EstabelecimentoDTO estabelecimento,
    
    @JsonProperty("itensNota")
    List<ItemNotaDTO> itensNota,
    
    @JsonProperty("nota")
    NotaDTO nota
) {
    public DadosNotaResponseDTO() {
        this(new EstabelecimentoDTO(), new ArrayList<>(), new NotaDTO());
    }
    
    public static Builder builder() {
        return new Builder();
    }
    
    public static class Builder {
        private EstabelecimentoDTO estabelecimento = new EstabelecimentoDTO();
        private List<ItemNotaDTO> itensNota = new ArrayList<>();
        private NotaDTO nota = new NotaDTO();
        
        public Builder estabelecimento(EstabelecimentoDTO estabelecimento) {
            this.estabelecimento = estabelecimento != null ? estabelecimento : new EstabelecimentoDTO();
            return this;
        }
        
        public Builder itensNota(List<ItemNotaDTO> itensNota) {
            this.itensNota = itensNota != null ? itensNota : new ArrayList<>();
            return this;
        }
        
        public Builder addItemNota(ItemNotaDTO item) {
            if (item != null) {
                this.itensNota.add(item);
            }
            return this;
        }
        
        public Builder nota(NotaDTO nota) {
            this.nota = nota != null ? nota : new NotaDTO();
            return this;
        }
        
        public DadosNotaResponseDTO build() {
            return new DadosNotaResponseDTO(estabelecimento, itensNota, nota);
        }
    }
}
