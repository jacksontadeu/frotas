package br.com.novotriunfo.frotas.entity.dto.response;

import br.com.novotriunfo.frotas.entity.enums.Servico;
import br.com.novotriunfo.frotas.entity.enums.StatusManutencao;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.time.LocalDate;
import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ManutencaoDTOResponse {
    private Long id;
    private LocalDate dataAgendamento;
    private LocalDate dataRealizacao;
    private String numeroManutencao;
    private Long kilometragem;
    private String tipoManutencao;
    private String descricao;
    private VeiculoDTOResponse veiculo;
    private StatusManutencao status;
    private Set<Servico> servicos;
}
