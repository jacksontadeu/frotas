package br.com.novotriunfo.frotas.entity.model;

import br.com.novotriunfo.frotas.entity.enums.TipoManutencao;
import br.com.novotriunfo.frotas.entity.enums.StatusManutencao;
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
    @JoinColumn(name="veiculo_id")
    private Veiculo veiculo;

    @Enumerated(EnumType.STRING)
    private StatusManutencao status = StatusManutencao.EM_ABERTO;

    private Boolean trocaOleo = false;
    private Boolean revisaoArrefecimento = false;
    private Boolean revisaoFreios = false;
    private Boolean embreagem = false;
    private Boolean faroisLampadas = false;
}
