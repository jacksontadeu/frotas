package br.com.novotriunfo.frotas.entity.dto.request;

import br.com.novotriunfo.frotas.entity.enums.Servico;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AtendimentoDTORequest {
    private Set<Servico> servicos = new HashSet<>();
    private Long kilometragem;
    private LocalDate dataRealizacao;
    private String descricao;
}
