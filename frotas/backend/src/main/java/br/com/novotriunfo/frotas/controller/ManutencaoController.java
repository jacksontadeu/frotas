package br.com.novotriunfo.frotas.controller;

import br.com.novotriunfo.frotas.entity.dto.request.ManutencaoDTORequest;
import br.com.novotriunfo.frotas.entity.model.Manutencao;
import br.com.novotriunfo.frotas.entity.model.Veiculo;
import br.com.novotriunfo.frotas.service.ManutencaoService;
import br.com.novotriunfo.frotas.service.VeiculoService;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.modelmapper.ModelMapper;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;

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
        Manutencao manutencao= modelMapper.map(request, Manutencao.class);
        Optional<Veiculo> veiculo = veiculoService.buscarPorId(request.getVeiculo_id());
        manutencao.setVeiculo(veiculo.get());
        manutencaoService.cadastrarMauntencao(manutencao);
        return ResponseEntity.status(HttpStatus.CREATED).body("Manutenção cadastrada com sucesso!!!");
    }
}
