package br.com.novotriunfo.frotas.repository;

import br.com.novotriunfo.frotas.entity.model.Base;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface BaseRepository extends JpaRepository<Base, Long> {

    Optional<Base> findById(Long id);


}
