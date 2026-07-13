package br.com.novotriunfo.frotas.controller;

import br.com.novotriunfo.frotas.entity.dto.request.BaseDTORequest;
import br.com.novotriunfo.frotas.entity.dto.response.BaseDTOEntityResponse;
import br.com.novotriunfo.frotas.entity.dto.response.BaseDTOResponse;
import br.com.novotriunfo.frotas.entity.model.Base;
import br.com.novotriunfo.frotas.service.BaseService;
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

@RestController
@RequestMapping("/base")
@Tag(name="base")
public class BaseController {

    private final BaseService baseService;
    private final ModelMapper modelMapper;

    public BaseController(BaseService baseService, ModelMapper modelMapper) {
        this.baseService = baseService;
        this.modelMapper = modelMapper;
    }
    @PostMapping
    public ResponseEntity cadastarBase(@Valid @RequestBody BaseDTORequest request){
        Base base = modelMapper.map(request, Base.class);
        baseService.cadastrarBase(base);
        return ResponseEntity.status(HttpStatus.CREATED).body("Cadastrado com sucesso!!!");

    }
    @GetMapping("/{id}")
    public ResponseEntity<BaseDTOResponse> buscarPorId(@PathVariable Long id){
        Optional<Base> base = baseService.buscarPorId(id);
        if(base.isPresent()){
            BaseDTOResponse response = modelMapper.map(base.get(), BaseDTOResponse.class);
            return ResponseEntity.status(HttpStatus.OK).body(response);
        }else{
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }

    }
    @GetMapping()
    public ResponseEntity<List<BaseDTOEntityResponse>> listarTodas(){
        List<Base> bases = baseService.listarTodas();
        List<BaseDTOEntityResponse> lista = modelMapper.map(bases,new TypeToken<List<BaseDTOEntityResponse>>(){}.getType());
        return  ResponseEntity.status(HttpStatus.OK).body(lista);
    }
}
