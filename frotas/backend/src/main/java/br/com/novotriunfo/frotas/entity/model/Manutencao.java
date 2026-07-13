package br.com.novotriunfo.frotas.entity.model;

import br.com.novotriunfo.frotas.entity.enums.TipoManutencao;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Manutencao {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private LocalDate dataRealizacao;
    private LocalDate dataProximaManutencao;
    private Long kilometragem;
    @Enumerated(EnumType.STRING)
    private TipoManutencao tipoManutencao;

    @ManyToOne
    @JoinColumn(name=("veiculo_id"))
    private Veiculo veiculo;
}
