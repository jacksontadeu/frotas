package br.com.novotriunfo.frotas.service;

import br.com.novotriunfo.frotas.entity.dto.request.VeiculoDTORequest;
import br.com.novotriunfo.frotas.entity.enums.TipoVeiculo;
import br.com.novotriunfo.frotas.entity.model.Base;
import br.com.novotriunfo.frotas.entity.model.Veiculo;
import br.com.novotriunfo.frotas.repository.BaseRepository;
import br.com.novotriunfo.frotas.repository.VeiculoRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.Year;
import java.util.List;
import java.util.Optional;

@Service
public class VeiculoService {

    private final VeiculoRepository veiculoRepository;
    private final BaseRepository baseRepository;

    public VeiculoService(VeiculoRepository veiculoRepository, BaseRepository baseRepository) {
        this.veiculoRepository = veiculoRepository;
        this.baseRepository = baseRepository;
    }

    public void cadastrarVeiculo(VeiculoDTORequest request) {
        Veiculo veiculo = new Veiculo();
        if (veiculoRepository.existsByPlacaVeiculo(request.getPlacaVeiculo())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Placa já cadastrada.");
        }
        Optional<Base> base = baseRepository.findById(request.getBase_id());
        veiculo.setPlacaVeiculo(request.getPlacaVeiculo().toUpperCase());
        veiculo.setBase(base.get());
        veiculo.setTipoVeiculo(TipoVeiculo.valueOf(request.getTipoVeiculo()));
        veiculo.setCor(request.getCor());
        veiculo.setAnoDeFabricacao(Year.parse(String.valueOf(request.getAnoDeFabricacao())));
        veiculo.setNome(request.getNome());
        veiculo.setFrota(request.getFrota());
        veiculoRepository.save(veiculo);
    }

    public List<Veiculo> listarTodos() {
        return veiculoRepository.findAll();
    }

    public Optional<Veiculo> buscarPorId(Long id) {
        return Optional.ofNullable(veiculoRepository.findById(id).orElse(null));

    }

    public Boolean verificarPlaca(String placa) {
        return veiculoRepository.existsByPlacaVeiculo(placa.toUpperCase());
    }
    public Boolean verificarFrota(String frota) {
        return veiculoRepository.existsByFrota(frota.toUpperCase());
    }
}