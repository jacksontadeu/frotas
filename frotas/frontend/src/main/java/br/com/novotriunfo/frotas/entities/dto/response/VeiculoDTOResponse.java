package br.com.novotriunfo.frotas.entities.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class VeiculoDTOResponse {
    private Long id;
    private String nome;
    private String cor;
    private String placaVeiculo;
    private String anoDeFabricacao;
    private String frota;
    private String tipoVeiculo;
    private BaseDTOEntityResponse base;
}
