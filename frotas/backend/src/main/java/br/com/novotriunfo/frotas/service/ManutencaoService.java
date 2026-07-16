package br.com.novotriunfo.frotas.service;

import br.com.novotriunfo.frotas.entity.model.Manutencao;
import br.com.novotriunfo.frotas.entity.enums.StatusManutencao;
import br.com.novotriunfo.frotas.repository.ManutencaoRepository;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

@Service
public class ManutencaoService {
    private final ManutencaoRepository manutencaoRepository;

    public ManutencaoService(ManutencaoRepository manutencaoRepository) {
        this.manutencaoRepository = manutencaoRepository;
    }

    public void cadastrarMauntencao(Manutencao manutencao){
        manutencaoRepository.save(manutencao);
    }

    public Optional<Manutencao> buscarPorId(Long id) {
        return manutencaoRepository.findById(id);
    }

    public List<Manutencao> listarTodas() {
        return manutencaoRepository.findAll();
    }

    public List<Manutencao> listarPorStatus(StatusManutencao status) {
        return manutencaoRepository.findByStatus(status);
    }

    public void salvar(Manutencao manutencao) {
        manutencaoRepository.save(manutencao);
    }
}
