package br.com.novotriunfo.frotas.controller;

import br.com.novotriunfo.frotas.entity.model.Veiculo;
import br.com.novotriunfo.frotas.service.VeiculoService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/veiculo")
public class VeiculoController {

    private final VeiculoService veiculoService;

    public VeiculoController(VeiculoService veiculoService) {
        this.veiculoService = veiculoService;
    }

    @PostMapping()
    public ResponseEntity cadastrarVeiuclo(@PathVariable Veiculo veiculo){
        veiculoService.cadastrarVeiculo(veiculo);
        return ResponseEntity.status(HttpStatus.CREATED).body("Cadastrado com sucesso!!!");
    }
}
