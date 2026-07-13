package br.com.novotriunfo.frotas.repository;

import br.com.novotriunfo.frotas.entity.model.Manutencao;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ManutencaoRepository extends JpaRepository<Manutencao,Long> {

}
