package br.com.novotriunfo.frotas.entity.dto.request;

public record UsuarioDTORequest(String nome,
                                String senha,
                                String email,
                                String role) {

}
