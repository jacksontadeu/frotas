package br.com.novotriunfo.frotas.controller;

import br.com.novotriunfo.frotas.entity.dto.request.VeiculoDTORequest;
import br.com.novotriunfo.frotas.entity.dto.response.BaseDTOEntityResponse;
import br.com.novotriunfo.frotas.entity.dto.response.VeiculoDTOResponse;
import br.com.novotriunfo.frotas.entity.model.Base;
import br.com.novotriunfo.frotas.entity.model.Veiculo;
import br.com.novotriunfo.frotas.service.BaseService;
import br.com.novotriunfo.frotas.service.VeiculoService;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.apache.coyote.Response;
import org.modelmapper.ModelMapper;
import org.modelmapper.TypeToken;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;
@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/veiculo")
@Tag(name = "veiculo")
public class VeiculoController {

    private final VeiculoService veiculoService;
    private final ModelMapper modelMapper;
    private final BaseService baseService;

    public VeiculoController(VeiculoService veiculoService, ModelMapper modelMapper, BaseService baseService) {
        this.veiculoService = veiculoService;
        this.modelMapper = modelMapper;
        this.baseService = baseService;
    }

    @PostMapping()
    public ResponseEntity<String> cadastrarVeiculo(@RequestBody VeiculoDTORequest request) {
        veiculoService.cadastrarVeiculo(request);
        return ResponseEntity.status(HttpStatus.CREATED).body("Cadastrado com sucesso!!!");
    }

    @GetMapping()
    public ResponseEntity<List<VeiculoDTOResponse>> listarTodos() {
        List<VeiculoDTOResponse> veiculos = veiculoService.listarTodos();
        return ResponseEntity.status(HttpStatus.OK).body(veiculos);

    }

    @GetMapping("/{id}")
    public ResponseEntity<VeiculoDTOResponse> buscarPorId(@PathVariable Long id) {
        VeiculoDTOResponse veiculo = veiculoService.buscarPorId(id);
        return ResponseEntity.status(HttpStatus.OK).body(veiculo);
    }

    @GetMapping("/placa/{placa}")
    public ResponseEntity<Boolean> verificarPlaca(@PathVariable String placa) {
        return ResponseEntity.status(HttpStatus.OK).body(veiculoService.verificarPlaca(placa));
    }

    @GetMapping("/frota/{frota}")
    public ResponseEntity<Boolean> verificarFrota(@PathVariable String frota) {
        return ResponseEntity.status(HttpStatus.OK).body(veiculoService.verificarFrota(frota));
    }
    @PatchMapping("/kilometragem")
    public ResponseEntity<String> atualizarKilometragem(@RequestParam Long veiculoId, @RequestParam Long novaKilometragem) {
        veiculoService.atualizarKilometragem(veiculoId, novaKilometragem);
        return ResponseEntity.status(HttpStatus.OK).body("Kilometragem atualizada com sucesso!!!");
    }
    @PatchMapping("/base")
    public ResponseEntity<String> alterarBaseVeiculo(@RequestParam Long veiculoId, @RequestParam Long baseId) {
        veiculoService.alterarBaseVeiculo(veiculoId, baseId);
        return ResponseEntity.status(HttpStatus.OK).body("Base do veículo atualizada com sucesso!!!");
    }
}
