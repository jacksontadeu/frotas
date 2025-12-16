package br.com.novotriunfo.frotas.entity.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class VeiculoDTORequest {
    @NotBlank
    @NotNull
    private String nome;

    private String placaVeiculo;

    @NotBlank
    @NotNull
    private String frota;

    private Long base_id;
    private String tipoVeiculo;
    @NotBlank
    @NotNull
    private String cor;
    @NotBlank
    @NotNull
    private Integer anoDeFabricacao;
}
