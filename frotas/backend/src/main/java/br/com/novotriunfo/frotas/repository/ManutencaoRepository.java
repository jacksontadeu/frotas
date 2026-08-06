package br.com.novotriunfo.frotas.repository;

import br.com.novotriunfo.frotas.entity.model.Manutencao;
import br.com.novotriunfo.frotas.entity.enums.StatusManutencao;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ManutencaoRepository extends JpaRepository<Manutencao, Long> {
    List<Manutencao> findByStatus(StatusManutencao status);
    List<Manutencao> findByVeiculoIdIn(List<Long> veiculoIds);
}
