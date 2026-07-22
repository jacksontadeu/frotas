package br.com.novotriunfo.frotas.service;

import br.com.novotriunfo.frotas.entity.dto.request.UsuarioDTORequest;
import br.com.novotriunfo.frotas.entity.enums.Role;
import br.com.novotriunfo.frotas.entity.model.Usuario;
import br.com.novotriunfo.frotas.repository.UsuarioRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

@Service
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;

    public UsuarioService(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    public Usuario cadastrarUsuario(UsuarioDTORequest request) {
        Usuario usuario = new Usuario();
        usuario.setNome(request.nome());
        usuario.setEmail(request.email());
        usuario.setTelefone(request.telefone());
        String senhaEncr = new BCryptPasswordEncoder().encode(request.senha());
        usuario.setSenha(senhaEncr);
        usuario.setRole(Role.valueOf(request.role()));
        return usuarioRepository.save(usuario);
    }

    public Usuario atualizarUsuario(Long id, UsuarioDTORequest request) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuário não encontrado."));

        usuario.setNome(request.nome());
        usuario.setEmail(request.email());
        usuario.setTelefone(request.telefone());

        if (request.senha() != null && !request.senha().isBlank()) {
            String senhaEncr = new BCryptPasswordEncoder().encode(request.senha());
            usuario.setSenha(senhaEncr);
        }

        if (request.role() != null && !request.role().isBlank()) {
            usuario.setRole(Role.valueOf(request.role()));
        }

        return usuarioRepository.save(usuario);
    }

    public void excluirUsuario(Long id) {
        if (!usuarioRepository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuário não encontrado.");
        }
        usuarioRepository.deleteById(id);
    }

    public List<Usuario> listarTodos() {
        return usuarioRepository.findAll();
    }

    public Optional<Usuario> buscarPorId(Long id) {
        return usuarioRepository.findById(id);
    }
}
