package br.com.novotriunfo.frotas.entity.dto.request;

import jakarta.persistence.Column;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class BaseDTORequest {
    @NotBlank
    @NotNull
    private String nome;

    @NotBlank
    @NotNull
    private String localidade;
    @Column(name="email_base")
    private String emailBase;
}
