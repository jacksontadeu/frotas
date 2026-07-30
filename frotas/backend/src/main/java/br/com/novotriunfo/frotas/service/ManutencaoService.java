package br.com.novotriunfo.frotas.service;

import br.com.novotriunfo.frotas.entity.dto.request.AtendimentoDTORequest;
import br.com.novotriunfo.frotas.entity.dto.request.ManutencaoDTORequest;
import br.com.novotriunfo.frotas.entity.dto.response.ManutencaoDTOResponse;
import br.com.novotriunfo.frotas.entity.enums.TipoManutencao;
import br.com.novotriunfo.frotas.entity.model.Manutencao;
import br.com.novotriunfo.frotas.entity.enums.StatusManutencao;
import br.com.novotriunfo.frotas.entity.model.Veiculo;
import br.com.novotriunfo.frotas.repository.ManutencaoRepository;
import br.com.novotriunfo.frotas.repository.VeiculoRepository;
import org.modelmapper.ModelMapper;
import org.modelmapper.TypeToken;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class ManutencaoService {
    private final ManutencaoRepository manutencaoRepository;
    private final VeiculoRepository veiculoRepository;
    private final ModelMapper modelMapper;

    public ManutencaoService(ManutencaoRepository manutencaoRepository, VeiculoRepository veiculoRepository, ModelMapper modelMapper) {
        this.manutencaoRepository = manutencaoRepository;
        this.veiculoRepository = veiculoRepository;
        this.modelMapper = modelMapper;
    }

    public void cadastrarMauntencao(ManutencaoDTORequest request) {
        if(request.getDataAgendamento().isBefore(LocalDate.now())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "A data de agendamento não pode ser anterior a data atual");
        }
        Manutencao manutencao = modelMapper.map(request, Manutencao.class);
        manutencao.setTipoManutencao(TipoManutencao.valueOf(String.valueOf(request.getTipoManutencao())));
        Optional<Veiculo> veiculo = veiculoRepository.findById(request.getVeiculo_id());
        manutencao.setVeiculo(veiculo.get());

        manutencaoRepository.save(manutencao);
        String numero = String.format("MAN-%04d", manutencao.getId());
        manutencao.setNumeroManutencao(numero);
        manutencaoRepository.save(manutencao);
    }

    public ManutencaoDTOResponse buscarPorId(Long id) {
        Optional<Manutencao> manutencao = manutencaoRepository.findById(id);
        if (manutencao.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Manutenção não encontrada");
        }
        ManutencaoDTOResponse response = modelMapper.map(manutencao.get(), ManutencaoDTOResponse.class);
        return response;
    }

    public List<ManutencaoDTOResponse> listarTodas() {
        List<Manutencao> manutencoes = manutencaoRepository.findAll();
        List<ManutencaoDTOResponse> responses = modelMapper.map(manutencoes,
                new TypeToken<List<ManutencaoDTOResponse>>() {
                }.getType());
        return responses;
    }

    public List<ManutencaoDTOResponse> listarPorStatus(StatusManutencao status) {
        List<Manutencao> manutencoes = manutencaoRepository.findByStatus(status);
        List<ManutencaoDTOResponse> responses = modelMapper.map(manutencoes,
                new TypeToken<List<ManutencaoDTOResponse>>() {
                }.getType());
        return responses;
    }


}



