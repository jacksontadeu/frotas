package br.com.novotriunfo.frotas.entity.dto.request;

import br.com.novotriunfo.frotas.entity.enums.TipoManutencao;
import br.com.novotriunfo.frotas.entity.model.Veiculo;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ManutencaoDTORequest {

    private LocalDate dataRealizacao;

    private Long kilometragem;

    private String tipoManutencao;

   private Long veiculo_id;
}
