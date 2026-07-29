package br.com.novotriunfo.frotas.entity.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@ToString(exclude = {"responsavel", "veiculos"})
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Base {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;
    private String nome;

    @ManyToOne
    @JoinColumn(name = "usuario_responsavel_id")
    private Usuario responsavel;

    @OneToMany(mappedBy = "base")
    private Set<Veiculo> veiculos= new HashSet<>();

}