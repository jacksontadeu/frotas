package br.com.novotriunfo.frotas.entity.dto.response;

import br.com.novotriunfo.frotas.entity.enums.StatusManutencao;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ManutencaoDTOResponse {
    private Long id;
    private LocalDate dataRealizacao;
    private LocalDate dataProximaManutencao;
    private Long kilometragem;
    private String tipoManutencao;
    private VeiculoDTOResponse veiculo;
    private StatusManutencao status;
    private Boolean trocaOleo;
    private Boolean revisaoArrefecimento;
    private Boolean revisaoFreios;
    private Boolean embreagem;
    private Boolean faroisLampadas;
}
