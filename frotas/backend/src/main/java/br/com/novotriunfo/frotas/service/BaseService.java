package br.com.novotriunfo.frotas.service;

import br.com.novotriunfo.frotas.entity.model.Base;
import br.com.novotriunfo.frotas.repository.BaseRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class BaseService {
    private final BaseRepository baseRepository;

    public BaseService(BaseRepository baseRepository) {
        this.baseRepository = baseRepository;
    }

    public void cadastrarBase(Base base){
        baseRepository.save(base);
    }
    public Optional<Base> buscarPorId(Long id){
        return Optional.ofNullable(baseRepository.findById(id).orElse(null));

    }
    public List<Base> listarTodas(){

        return baseRepository.findAll();

    }
}
