package br.com.novotriunfo.frotas.controller;

import br.com.novotriunfo.frotas.entity.dto.request.BaseDTORequest;
import br.com.novotriunfo.frotas.entity.dto.response.BaseDTOEntityResponse;
import br.com.novotriunfo.frotas.entity.dto.response.BaseDTOResponse;
import br.com.novotriunfo.frotas.entity.model.Base;
import br.com.novotriunfo.frotas.entity.model.Usuario;
import br.com.novotriunfo.frotas.service.BaseService;
import br.com.novotriunfo.frotas.service.UsuarioService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.modelmapper.ModelMapper;
import org.modelmapper.TypeToken;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
@CrossOrigin(origins = "*")
@RestController
@RequestMapping("/base")
@Tag(name="base")
public class BaseController {

    private final BaseService baseService;
    private final UsuarioService usuarioService;
    private final ModelMapper modelMapper;

    public BaseController(BaseService baseService, UsuarioService usuarioService, ModelMapper modelMapper) {
        this.baseService = baseService;
        this.usuarioService = usuarioService;
        this.modelMapper = modelMapper;
    }
    @PostMapping
    public ResponseEntity cadastarBase(@Valid @RequestBody BaseDTORequest request){
        baseService.cadastrarBase(request);
        return ResponseEntity.status(HttpStatus.CREATED).body("Cadastrado com sucesso!!!");

    }
    @GetMapping("/{id}")
    public ResponseEntity<BaseDTOResponse> buscarPorId(@PathVariable Long id){
        BaseDTOResponse response = baseService.buscarPorId(id);
        return ResponseEntity.status(HttpStatus.OK).body(response);

    }
    @GetMapping()
    public ResponseEntity<List<BaseDTOResponse>> listarTodas(){
        List<BaseDTOResponse> bases = baseService.listarTodas();
        return ResponseEntity.status(HttpStatus.OK).body(bases);
    }
    @PutMapping("/{id}")
    public ResponseEntity alterarBase(@PathVariable Long id, @Valid @RequestBody BaseDTORequest request){
        baseService.alterarBase(id, request);
        return ResponseEntity.status(HttpStatus.OK).body("Alterado com sucesso!!!");
    }
}
