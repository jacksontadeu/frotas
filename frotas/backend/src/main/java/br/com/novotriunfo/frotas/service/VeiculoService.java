package br.com.novotriunfo.frotas.service;

import br.com.novotriunfo.frotas.entity.model.Veiculo;
import br.com.novotriunfo.frotas.repository.VeiculoRepository;
import org.springframework.stereotype.Service;

@Service
public class VeiculoService {

    private final VeiculoRepository veiculoRepository;

    public VeiculoService(VeiculoRepository veiculoRepository) {
        this.veiculoRepository = veiculoRepository;
    }

    public void cadastrarVeiculo(Veiculo veiculo){
        veiculoRepository.save(veiculo);
    }
}
