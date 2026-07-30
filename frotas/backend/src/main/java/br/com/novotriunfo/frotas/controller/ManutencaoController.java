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
@RequestMapping("/manutencao")
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
        manutencaoService.cadastrarMauntencao(request);
        return ResponseEntity.status(HttpStatus.CREATED).body("Manutenção cadastrada com sucesso!!!");
    }

    @GetMapping
    public ResponseEntity<List<ManutencaoDTOResponse>> listarManutencoes() {

       List<ManutencaoDTOResponse> responseList = manutencaoService.listarTodas();
        return ResponseEntity.ok(responseList);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ManutencaoDTOResponse> buscarManutencaoPorId(@PathVariable Long id) {
        ManutencaoDTOResponse response = manutencaoService.buscarPorId(id);
        return ResponseEntity.ok(response);
    }
    @GetMapping("/status")
    public ResponseEntity<List<ManutencaoDTOResponse>> listarManutencoesPorStatus(@RequestParam StatusManutencao status) {
        List<ManutencaoDTOResponse> responseList = manutencaoService.listarPorStatus(status);
        return ResponseEntity.ok(responseList);
    }
}
