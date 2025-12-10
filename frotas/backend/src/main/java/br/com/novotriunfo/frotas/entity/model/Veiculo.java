package br.com.novotriunfo.frotas.entity.model;

import br.com.novotriunfo.frotas.entity.enums.TipoVeiculo;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Data
public class Veiculo {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String nome;
    private String frota;

    @OneToOne
    @JoinColumn(name = "base_id")
    private Base base;

    @Enumerated(EnumType.STRING)
    private TipoVeiculo tipoVeiculo;
}
