package br.com.novotriunfo.frotas.entity.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Data
public class Base {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String nome;
    private String localidade;

    @ManyToOne
    @JoinColumn(name = "veiculos_id")
    private Veiculo veiculos;

}
