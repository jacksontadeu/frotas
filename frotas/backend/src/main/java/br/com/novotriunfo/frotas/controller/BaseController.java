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
        Optional<Usuario> usuario = usuarioService.buscarPorId(request.getUsuarioId());
        if (usuario.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Usuário não encontrado");
        }
        Base base = new Base();
        base.setNome(request.getNome());
        base.setEmailBase(usuario.get().getEmail());
        base.setTelefone(usuario.get().getTelefone());
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
