package br.com.novotriunfo.frotas.entity.dto.request;

import br.com.novotriunfo.frotas.entity.enums.Servico;
import br.com.novotriunfo.frotas.entity.enums.TipoManutencao;
import br.com.novotriunfo.frotas.entity.model.Veiculo;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ManutencaoDTORequest {

    private LocalDate dataAgendamento;

    private String tipoManutencao;

    private Long veiculo_id;

    private Set<Servico> servicos = new HashSet<>();
}
