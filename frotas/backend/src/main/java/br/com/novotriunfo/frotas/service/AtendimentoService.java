package br.com.novotriunfo.frotas.service;

import br.com.novotriunfo.frotas.entity.dto.request.AtendimentoDTORequest;
import br.com.novotriunfo.frotas.entity.enums.StatusManutencao;
import br.com.novotriunfo.frotas.entity.model.Manutencao;
import br.com.novotriunfo.frotas.entity.model.Veiculo;
import br.com.novotriunfo.frotas.repository.ManutencaoRepository;
import br.com.novotriunfo.frotas.repository.VeiculoRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

@Service
public class AtendimentoService {

    private final ManutencaoRepository manutencaoRepository;
    private final VeiculoRepository veiculoRepository;

    public AtendimentoService(ManutencaoRepository manutencaoRepository, VeiculoRepository veiculoRepository) {
        this.manutencaoRepository = manutencaoRepository;
        this.veiculoRepository = veiculoRepository;
    }


    public void realizarAtendimento(Long id, AtendimentoDTORequest request) {
        Optional<Manutencao> manutencaoOpt = manutencaoRepository.findById(id);
        if (manutencaoOpt.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Manutenção não encontrada");
        }
        Manutencao manutencao = manutencaoOpt.get();
        manutencao.setNumeroManutencao(manutencaoOpt.get().getNumeroManutencao());
        manutencao.setServicos(request.getServicos());
        manutencao.setKilometragem(request.getKilometragem());
        manutencao.setDataRealizacao(request.getDataRealizacao());
        manutencao.setStatus(StatusManutencao.FINALIZADA);
        Veiculo veiculo = manutencao.getVeiculo();
        if (request.getKilometragem() < veiculo.getKilometragemAtual()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "A quilometragem informada não pode ser menor que a quilometragem atual do veículo");
        }
        veiculo.setKilometragemAtual(request.getKilometragem());
        manutencaoRepository.save(manutencao);
        veiculoRepository.save(veiculo);
    }
}
