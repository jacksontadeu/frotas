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

import java.util.List;
import java.util.Optional;

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
    public ResponseEntity cadastrarVeiuclo(@RequestBody VeiculoDTORequest request){
        Veiculo veiculo = modelMapper.map(request, Veiculo.class);
        Optional<Base> base = baseService.buscarPorId(request.getBase_id());
        veiculo.setBase(base.get());
        veiculoService.cadastrarVeiculo(veiculo);
        return ResponseEntity.status(HttpStatus.CREATED).body("Cadastrado com sucesso!!!");
    }
    @GetMapping()
    public ResponseEntity<List<VeiculoDTOResponse>> listarTodos(){
        List<Veiculo> veiculos = veiculoService.listarTodos();
        List<VeiculoDTOResponse> lista = modelMapper.map(veiculos, new TypeToken<List<VeiculoDTOResponse>>(){}.getType());
        return ResponseEntity.status(HttpStatus.OK).body(lista);
    }
    @GetMapping("/{id}")
    public ResponseEntity<VeiculoDTOResponse> buscarPorId(@PathVariable Long id){
        Optional<Veiculo> veiculoEntity = veiculoService.buscarPorId(id);
        if(veiculoEntity.isPresent()){
            VeiculoDTOResponse veiculo = modelMapper.map(veiculoEntity, VeiculoDTOResponse.class);
            return ResponseEntity.status(HttpStatus.OK).body(veiculo);
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
    }
}
