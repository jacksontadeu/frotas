package br.com.novotriunfo.frotas.repository;

import br.com.novotriunfo.frotas.entity.model.Veiculo;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface VeiculoRepository extends JpaRepository<Veiculo, Long> {

    Optional<Veiculo> findById(Long id);

}
