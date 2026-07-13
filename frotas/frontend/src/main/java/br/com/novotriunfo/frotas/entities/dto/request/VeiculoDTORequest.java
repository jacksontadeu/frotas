package br.com.novotriunfo.frotas.entities.dto.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class VeiculoDTORequest {
    private String nome;
    private String placaVeiculo;
    private String frota;
    private Long base_id;
    private String tipoVeiculo;
    private String cor;
    private Integer anoDeFabricacao;
}
