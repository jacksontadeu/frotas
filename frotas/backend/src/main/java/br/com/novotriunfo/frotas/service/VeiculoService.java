package br.com.novotriunfo.frotas.service;

import br.com.novotriunfo.frotas.entity.dto.request.VeiculoDTORequest;
import br.com.novotriunfo.frotas.entity.dto.response.BaseDTOResponse;
import br.com.novotriunfo.frotas.entity.dto.response.VeiculoDTOResponse;
import br.com.novotriunfo.frotas.entity.enums.TipoVeiculo;
import br.com.novotriunfo.frotas.entity.model.Base;
import br.com.novotriunfo.frotas.entity.model.Veiculo;
import br.com.novotriunfo.frotas.repository.BaseRepository;
import br.com.novotriunfo.frotas.repository.VeiculoRepository;
import org.modelmapper.ModelMapper;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.Year;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class VeiculoService {

    private final VeiculoRepository veiculoRepository;
    private final BaseRepository baseRepository;
    private final ModelMapper modelMapper;

    public VeiculoService(VeiculoRepository veiculoRepository, BaseRepository baseRepository, ModelMapper modelMapper) {
        this.veiculoRepository = veiculoRepository;
        this.baseRepository = baseRepository;
        this.modelMapper = modelMapper;
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

    public List<VeiculoDTOResponse> listarTodos() {
        List<Veiculo> veiculos = veiculoRepository.findAll();
        return veiculos.stream().map(veiculo -> {
            VeiculoDTOResponse response = new VeiculoDTOResponse();
            response.setId(veiculo.getId());
            response.setPlacaVeiculo(veiculo.getPlacaVeiculo());
            response.setBase(modelMapper.map(veiculo.getBase(), BaseDTOResponse.class));
            response.setTipoVeiculo(String.valueOf(veiculo.getTipoVeiculo()));
            response.setCor(veiculo.getCor());
            response.setAnoDeFabricacao(String.valueOf(veiculo.getAnoDeFabricacao()));
            response.setNome(veiculo.getNome());
            response.setFrota(veiculo.getFrota());
            return response;
        }).collect(Collectors.toList());
    }

    public VeiculoDTOResponse buscarPorId(Long id) {
        Optional<Veiculo> veiculo = veiculoRepository.findById(id);
        if (veiculo.isPresent()) {
            VeiculoDTOResponse response = modelMapper.map(veiculo.get(), VeiculoDTOResponse.class);
            return response;
        }
        throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Veículo não encontrado.");
    }

    public Boolean verificarPlaca(String placa) {
        Boolean existe = veiculoRepository.existsByPlacaVeiculo(placa.toUpperCase());
        if (existe){
            return true;
        }
        return false;
    }
    public Boolean verificarFrota(String frota) {
        Boolean existe = veiculoRepository.existsByFrota(frota.toUpperCase());
        if (existe){
            return true;
        }
        return false;
    }
}