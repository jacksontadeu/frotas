package br.com.novotriunfo.frotas.entity.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UsuarioDTOResponse {
    private Long id;
    private String nome;
    private String email;
    private String telefone;
    private String role;
}
