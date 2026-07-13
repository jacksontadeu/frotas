package br.com.novotriunfo.frotas.entity.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class BaseDTOEntityResponse {
    private Long id;
    private String nome;
    private String localidade;
    private String emailBase;
}
