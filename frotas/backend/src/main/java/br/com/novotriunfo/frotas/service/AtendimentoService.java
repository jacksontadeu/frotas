package br.com.novotriunfo.frotas.service;

import br.com.novotriunfo.frotas.entity.dto.request.AtendimentoDTORequest;
import br.com.novotriunfo.frotas.entity.enums.StatusManutencao;
import br.com.novotriunfo.frotas.entity.enums.TipoManutencao;
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
        if (manutencaoOpt.get().getTipoManutencao().equals(TipoManutencao.CORRETIVA)) {
            Manutencao manutencao = manutencaoOpt.get();
            manutencao.setNumeroManutencao(manutencaoOpt.get().getNumeroManutencao());
            manutencao.setDescricao(request.getDescricao());
            manutencao.setKilometragem(request.getKilometragem());
            manutencao.setDataRealizacao(request.getDataRealizacao());
            manutencao.setStatus(StatusManutencao.FINALIZADA);
            Veiculo veiculo = manutencao.getVeiculo();
            if (request.getKilometragem() < veiculo.getKilometragemAtual()) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "A quilometragem informada não pode ser menor que a quilometragem atual do veículo");
            }
            veiculo.setKilometragemAtual(request.getKilometragem());
            veiculo.setDataUltimaCorretiva(request.getDataRealizacao());
            veiculo.setKmCorretiva(request.getKilometragem());
            manutencaoRepository.save(manutencao);
            veiculoRepository.save(veiculo);
        } else if (manutencaoOpt.get().getTipoManutencao().equals(TipoManutencao.PREVENTIVA_TROCA_DE_OLEO)) {
            Manutencao manutencao = manutencaoOpt.get();
            manutencao.setNumeroManutencao(manutencaoOpt.get().getNumeroManutencao());
            manutencao.setServicos(request.getServicos());
            manutencao.setDescricao(null);
            manutencao.setKilometragem(request.getKilometragem());
            manutencao.setDataRealizacao(request.getDataRealizacao());
            manutencao.setStatus(StatusManutencao.FINALIZADA);
            Veiculo veiculo = manutencao.getVeiculo();
            if (request.getKilometragem() < veiculo.getKilometragemAtual()) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "A quilometragem informada não pode ser menor que a quilometragem atual do veículo");
            }
            veiculo.setDataUltimaTrocaOleo(request.getDataRealizacao());
            veiculo.setDataProximaTrocaOleo(request.getDataRealizacao().plusMonths(6));
            veiculo.setKilometragemAtual(request.getKilometragem());
            veiculo.setKmTrocaOleo(request.getKilometragem() + 10000L);
            manutencaoRepository.save(manutencao);
            veiculoRepository.save(veiculo);
            cadastrarTrocaOleo(veiculo, request);
        } else {
            Manutencao manutencao = manutencaoOpt.get();
            manutencao.setNumeroManutencao(manutencaoOpt.get().getNumeroManutencao());
            manutencao.setServicos(request.getServicos());
            manutencao.setDescricao(null);
            manutencao.setKilometragem(request.getKilometragem());
            manutencao.setDataRealizacao(request.getDataRealizacao());
            manutencao.setStatus(StatusManutencao.FINALIZADA);
            Veiculo veiculo = manutencao.getVeiculo();
            if (request.getKilometragem() < veiculo.getKilometragemAtual()) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "A quilometragem informada não pode ser menor que a quilometragem atual do veículo");
            }
            veiculo.setDataUltimaCorreiaDentada(request.getDataRealizacao());
            veiculo.setDataProximaCorreiaDentada(request.getDataRealizacao().plusMonths(24));
            veiculo.setKilometragemAtual(request.getKilometragem());
            veiculo.setKmTrocaCorreiaDentada(request.getKilometragem() + 50000L);
            manutencaoRepository.save(manutencao);
            veiculoRepository.save(veiculo);
            cadastrarTrocaCorreiaDentada(veiculo, request);

        }
    }

    private void cadastrarTrocaOleo(Veiculo veiculo, AtendimentoDTORequest request) {
        Manutencao manutencao = new Manutencao();
        manutencao.setVeiculo(veiculo);
        manutencao.setTipoManutencao(TipoManutencao.PREVENTIVA_TROCA_DE_OLEO);
        manutencao.setDataAgendamento(request.getDataRealizacao().plusMonths(6));

        manutencaoRepository.save(manutencao);
        String numero = String.format("MAN-%04d", manutencao.getId());
        manutencao.setNumeroManutencao(numero);
        manutencaoRepository.save(manutencao);
    }

    private void cadastrarTrocaCorreiaDentada(Veiculo veiculo, AtendimentoDTORequest request) {
        Manutencao manutencao = new Manutencao();
        manutencao.setVeiculo(veiculo);
        manutencao.setTipoManutencao(TipoManutencao.PREVENTIVA_KIT_CORREIA_DENTADA);
        manutencao.setDataAgendamento(request.getDataRealizacao().plusMonths(24));

        manutencaoRepository.save(manutencao);
        String numero = String.format("MAN-%04d", manutencao.getId());
        manutencao.setNumeroManutencao(numero);
        manutencaoRepository.save(manutencao);
    }
}
