package br.com.novotriunfo.frotas.controller;

import br.com.novotriunfo.frotas.entity.dto.request.AtendimentoDTORequest;
import br.com.novotriunfo.frotas.service.AtendimentoService;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/atendimento")
@Tag(name="atendimento")
public class AtendimentoController {
    private final AtendimentoService atendimentoService;

    public AtendimentoController(AtendimentoService atendimentoService) {
        this.atendimentoService = atendimentoService;
    }

    @PatchMapping("/{id}")
    public ResponseEntity<String> realizarAtendimento(@PathVariable Long id, @RequestBody AtendimentoDTORequest request) {
        atendimentoService.realizarAtendimento(id, request);
        return ResponseEntity.ok("Atendimento realizado com sucesso");
    }
}
