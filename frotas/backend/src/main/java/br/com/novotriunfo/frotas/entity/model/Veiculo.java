package br.com.novotriunfo.frotas.entity.model;

import br.com.novotriunfo.frotas.entity.enums.TipoVeiculo;
import jakarta.persistence.*;
import lombok.*;

import java.time.Year;
import java.util.ArrayList;
import java.util.List;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@ToString(exclude = {"base", "manutencoes"})
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Veiculo {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;
    private String nome;
    private String frota;
    private String placaVeiculo;
    private String cor;
    private Year anoDeFabricacao;

    private Boolean ativo = true;
    private String motivoInativacao;

    @Enumerated(EnumType.STRING)
    private TipoVeiculo tipoVeiculo;

    @ManyToOne
    @JoinColumn(name = "base_id")
    private Base base;

    @OneToMany(mappedBy = "veiculo")
    private List<Manutencao> manutencoes= new ArrayList<>();

}