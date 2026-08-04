package br.com.novotriunfo.frotas.entity.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class VeiculoDTOResponse {
    private Long id;
    private String nome;
    private String placaVeiculo;
    private String cor;
    private String anoDeFabricacao;
    private Long kilometragemAtual;
    private Long kmTrocaOleo;
    private Long kmTrocaCorreiaDentada;
    private LocalDate dataProximaTrocaOleo;
    private LocalDate dataProximaCorreiaDentada;
    private LocalDate dataUltimaTrocaOleo;
    private LocalDate dataUltimaCorreiaDentada;
    private LocalDate dataUltimaCorretiva;
    private Long kmCorretiva;
    private String frota;
    private String tipoVeiculo;
    private BaseDTOResponse base;
}
