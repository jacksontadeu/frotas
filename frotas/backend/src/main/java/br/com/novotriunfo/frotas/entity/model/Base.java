package br.com.novotriunfo.frotas.entity.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Data
public class Base {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String nome;
    private String telefone;

    @Column(name = "email_base")
    private String emailBase;

    @ManyToOne
    @JoinColumn(name = "usuario_responsavel_id")
    private Usuario responsavel;

    @OneToMany(mappedBy = ("base"))
    private Set<Veiculo> veiculos= new HashSet<>();

}
