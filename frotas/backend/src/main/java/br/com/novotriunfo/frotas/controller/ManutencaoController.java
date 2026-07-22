package br.com.novotriunfo.frotas.controller;

import br.com.novotriunfo.frotas.entity.dto.request.ManutencaoDTORequest;
import br.com.novotriunfo.frotas.entity.dto.request.AtendimentoDTORequest;
import br.com.novotriunfo.frotas.entity.dto.response.ManutencaoDTOResponse;
import br.com.novotriunfo.frotas.entity.model.Manutencao;
import br.com.novotriunfo.frotas.entity.model.Veiculo;
import br.com.novotriunfo.frotas.entity.enums.StatusManutencao;
import br.com.novotriunfo.frotas.service.ManutencaoService;
import br.com.novotriunfo.frotas.service.VeiculoService;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.modelmapper.ModelMapper;
import org.modelmapper.TypeToken;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
@CrossOrigin(origins = "*")
@RestController
@RequestMapping("manutencao")
@Tag(name="manutencao")
public class ManutencaoController {
    private final ModelMapper modelMapper;
    private final ManutencaoService manutencaoService;
    private final VeiculoService veiculoService;

    public ManutencaoController(ModelMapper modelMapper, ManutencaoService manutencaoService, VeiculoService veiculoService) {
        this.modelMapper = modelMapper;
        this.manutencaoService = manutencaoService;
        this.veiculoService = veiculoService;
    }

    @PostMapping
    public ResponseEntity cadastrarManutencao(@RequestBody ManutencaoDTORequest request){
        Manutencao manutencao = modelMapper.map(request, Manutencao.class);
        Optional<Veiculo> veiculo = veiculoService.buscarPorId(request.getVeiculo_id());
        manutencao.setVeiculo(veiculo.get());
        if (request.getServicos() != null && !request.getServicos().isEmpty()) {
            manutencao.setServicos(request.getServicos());
        }
        manutencao.setStatus(StatusManutencao.EM_ABERTO); // Garante que inicia em aberto
        manutencaoService.cadastrarMauntencao(manutencao);
        return ResponseEntity.status(HttpStatus.CREATED).body("Manutenção cadastrada com sucesso!!!");
    }

    @GetMapping
    public ResponseEntity<List<ManutencaoDTOResponse>> listarManutencoes(@RequestParam(required = false) StatusManutencao status) {
        List<Manutencao> manutencoes;
        if (status != null) {
            manutencoes = manutencaoService.listarPorStatus(status);
        } else {
            manutencoes = manutencaoService.listarTodas();
        }
        List<ManutencaoDTOResponse> responseList = modelMapper.map(manutencoes, new TypeToken<List<ManutencaoDTOResponse>>(){}.getType());
        return ResponseEntity.ok(responseList);
    }

    @PutMapping("/{id}/atendimento")
    public ResponseEntity<?> atenderManutencao(@PathVariable Long id, @RequestBody AtendimentoDTORequest request) {
        Optional<Manutencao> optionalManutencao = manutencaoService.buscarPorId(id);
        if (optionalManutencao.isPresent()) {
            Manutencao manutencao = optionalManutencao.get();
            manutencao.setStatus(StatusManutencao.FINALIZADA);
            if (request.getServicos() != null) {
                manutencao.setServicos(request.getServicos());
            }
            if (request.getKilometragem() != null) {
                manutencao.setKilometragem(request.getKilometragem());
            }
            if (request.getDataRealizacao() != null) {
                manutencao.setDataRealizacao(request.getDataRealizacao());
            }
            // Define a data da próxima manutenção para 6 meses no futuro
            manutencao.setDataProximaManutencao(LocalDate.now().plusMonths(6));
            manutencaoService.salvar(manutencao);
            return ResponseEntity.ok().body("Atendimento de manutenção realizado com sucesso!");
        }
        return ResponseEntity.notFound().build();
    }
}
