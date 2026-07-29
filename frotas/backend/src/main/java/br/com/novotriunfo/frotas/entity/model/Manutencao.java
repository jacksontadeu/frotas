package br.com.novotriunfo.frotas.entity.model;

import br.com.novotriunfo.frotas.entity.enums.Servico;
import br.com.novotriunfo.frotas.entity.enums.TipoManutencao;
import br.com.novotriunfo.frotas.entity.enums.StatusManutencao;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Manutencao {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private LocalDate dataAgendamento;

    private LocalDate dataProximaManutencao;

    private Long kilometragem;
    
    @Enumerated(EnumType.STRING)
    private TipoManutencao tipoManutencao;

    @ManyToOne
    @JoinColumn(name="veiculo_id")
    private Veiculo veiculo;

    @Enumerated(EnumType.STRING)
    private StatusManutencao status = StatusManutencao.EM_ABERTO;

    @ElementCollection(targetClass = Servico.class, fetch = FetchType.EAGER)
    @CollectionTable(name = "manutencao_servicos",
            joinColumns = @JoinColumn(name = "manutencao_id"))
    @Enumerated(EnumType.STRING)
    @Column(name = "servico")
    private Set<Servico> servicos = new HashSet<>();
}
