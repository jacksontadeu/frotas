package br.com.novotriunfo.frotas.service;

import br.com.novotriunfo.frotas.entity.dto.request.BaseDTORequest;
import br.com.novotriunfo.frotas.entity.dto.response.BaseDTOResponse;
import br.com.novotriunfo.frotas.entity.model.Base;
import br.com.novotriunfo.frotas.entity.model.Usuario;
import br.com.novotriunfo.frotas.repository.BaseRepository;
import br.com.novotriunfo.frotas.repository.UsuarioRepository;
import org.modelmapper.ModelMapper;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class BaseService {
    private final BaseRepository baseRepository;
    private final UsuarioRepository usuarioRepository;
    private final ModelMapper modelMapper;

    public BaseService(BaseRepository baseRepository, UsuarioRepository usuarioRepository, ModelMapper modelMapper) {
        this.baseRepository = baseRepository;
        this.usuarioRepository = usuarioRepository;
        this.modelMapper = modelMapper;
    }

    public void cadastrarBase(BaseDTORequest request){
        Optional<Usuario> usuarioOptional = usuarioRepository.findById(request.getUsuarioId());
        if (usuarioOptional.isPresent()) {
            Base base = new Base();
            base.setNome(request.getNome().toUpperCase());
            base.setResponsavel(usuarioOptional.get());
            baseRepository.save(base);
        }else{
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuário não encontrado");
        }
    }
    public BaseDTOResponse buscarPorId(Long id){
        Optional<Base> baseOptional = baseRepository.findById(id);
        if (!baseOptional.isPresent()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Base não encontrada");
        }
        BaseDTOResponse response =new BaseDTOResponse();
        response.setId(baseOptional.get().getId());
        response.setNome(baseOptional.get().getNome());
        response.setTelefone(baseOptional.get().getResponsavel().getTelefone());
        response.setEmail(baseOptional.get().getResponsavel().getEmail());
        response.setResponsavel(baseOptional.get().getResponsavel().getNome());
        return response;
    }
    public List<BaseDTOResponse> listarTodas(){
        List<Base> bases = baseRepository.findAll();
        return bases.stream() .map(base -> {
                    BaseDTOResponse response = new BaseDTOResponse();
                    response.setId(base.getId());
                    response.setNome(base.getNome());
                    response.setTelefone(base.getResponsavel().getTelefone());
                    response.setEmail(base.getResponsavel().getEmail());
                    response.setResponsavel(base.getResponsavel().getNome());
                    return response;
                }).collect(Collectors.toList());
    }
    public void alterarBase(Long id, BaseDTORequest request){
        Optional<Base> baseOptional = baseRepository.findById(id);
        if (baseOptional.isPresent()) {
            Base base = baseOptional.get();
            base.setNome(request.getNome().toUpperCase());
           Optional<Usuario> usuarioOptional = usuarioRepository.findById(request.getUsuarioId());
            if (usuarioOptional.isPresent()) {
                base.setResponsavel(usuarioOptional.get());
                baseRepository.save(base);
            } else {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuário não encontrado");
            }
        } else {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Base não encontrada");
        }
    }
}