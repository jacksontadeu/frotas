package br.com.novotriunfo.frotas.entity.dto.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AtendimentoDTORequest {
    private Boolean trocaOleo;
    private Boolean revisaoArrefecimento;
    private Boolean revisaoFreios;
    private Boolean embreagem;
    private Boolean faroisLampadas;
}
