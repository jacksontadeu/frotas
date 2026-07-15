package br.com.novotriunfo.frotas.service;

import br.com.novotriunfo.frotas.entity.dto.request.UsuarioDTORequest;
import br.com.novotriunfo.frotas.entity.enums.Role;
import br.com.novotriunfo.frotas.entity.model.Usuario;
import br.com.novotriunfo.frotas.repository.UsuarioRepository;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;

    public UsuarioService(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    public void cadastrarUsuario(UsuarioDTORequest request) {
        Usuario usuario = new Usuario();
        usuario.setNome(request.nome());
        usuario.setEmail(request.email());
        String senhaEncr= new BCryptPasswordEncoder().encode(request.senha());
        usuario.setSenha(senhaEncr);
        usuario.setRole(Role.valueOf(request.role()));
        usuarioRepository.save(usuario);

    }

}
