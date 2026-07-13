package br.com.novotriunfo.frotas.entities.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class BaseDTOResponse {
    private Long id;
    private String nome;
    private String localizacao;
    List<VeiculoDTOResponse> veiculos;
}
