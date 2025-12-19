package br.com.novotriunfo.frotas.service;

import br.com.novotriunfo.frotas.entity.model.Manutencao;
import br.com.novotriunfo.frotas.repository.ManutencaoRepository;
import org.springframework.stereotype.Service;

@Service
public class ManutencaoService {
    private final ManutencaoRepository manutencaoRepository;

    public ManutencaoService(ManutencaoRepository manutencaoRepository) {
        this.manutencaoRepository = manutencaoRepository;
    }

    public void cadastrarMauntencao(Manutencao manutencao){
        manutencaoRepository.save(manutencao);
    }
}
