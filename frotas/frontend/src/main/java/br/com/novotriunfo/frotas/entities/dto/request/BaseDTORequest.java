package br.com.novotriunfo.frotas.entities.dto.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class BaseDTORequest {
    private String nome;
    private String localidade;
}
