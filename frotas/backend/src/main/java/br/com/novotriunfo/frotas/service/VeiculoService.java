package br.com.novotriunfo.frotas.service;

import br.com.novotriunfo.frotas.entity.dto.request.VeiculoDTORequest;
import br.com.novotriunfo.frotas.entity.model.Veiculo;
import br.com.novotriunfo.frotas.repository.VeiculoRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

@Service
public class VeiculoService {

    private final VeiculoRepository veiculoRepository;

    public VeiculoService(VeiculoRepository veiculoRepository) {
        this.veiculoRepository = veiculoRepository;
    }

    public void cadastrarVeiculo(Veiculo veiculo){
        if(veiculoRepository.existsByPlacaVeiculo(veiculo.getPlacaVeiculo())){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Placa já cadastrada.");
        }
        veiculoRepository.save(veiculo);
    }
    public List<Veiculo> listarTodos(){
        return veiculoRepository.findAll();
    }
    public Optional<Veiculo> buscarPorId(Long id){
        return Optional.ofNullable(veiculoRepository.findById(id).orElse(null));

    }
}
