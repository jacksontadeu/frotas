package br.com.novotriunfo.frotas.controller;

import br.com.novotriunfo.frotas.entity.dto.response.ManutencaoDTOResponse;
import br.com.novotriunfo.frotas.entity.dto.response.VeiculoDTOResponse;
import br.com.novotriunfo.frotas.entity.model.Veiculo;
import br.com.novotriunfo.frotas.service.ManutencaoService;
import br.com.novotriunfo.frotas.service.PdfService;
import br.com.novotriunfo.frotas.service.VeiculoService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/relatorios")
@Tag(name = "Relatório", description = "Endpoints para geração de relatórios em PDF")
public class RelatorioController {

    private final PdfService pdfService;
    private final VeiculoService veiculoService;
    private final ManutencaoService manutencaoService;

    public RelatorioController(PdfService pdfService, VeiculoService veiculoService, ManutencaoService manutencaoService) {
        this.pdfService = pdfService;
        this.veiculoService = veiculoService;
        this.manutencaoService = manutencaoService;
    }

    @GetMapping("/veiculos/listartodos")
    public ResponseEntity<byte[]> gerarRelatorioExemplo() {
        List<VeiculoDTOResponse> veiculos = veiculoService.listarTodos();

        Map<String, List<VeiculoDTOResponse>> veiculosPorBase = veiculos.stream()
                .collect(Collectors.groupingBy(
                        v -> (v.getBase() != null && v.getBase().getNome() != null) ? v.getBase().getNome() : "Sem Base Definida"
                ));

        Map<String, Object> dados = new HashMap<>();
        dados.put("veiculosPorBase", veiculosPorBase);

        byte[] pdfBytes = pdfService.gerarPdf("listarVeiculos", dados);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.add(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=relatorio-exemplo.pdf");

        return ResponseEntity.ok()
                .headers(headers)
                .body(pdfBytes);
    }

    @GetMapping("/veiculos/{id}")
    public ResponseEntity<byte[]> gerarRelatorioVeiculosDetalhado(@PathVariable Long id) {

        VeiculoDTOResponse veiculo = veiculoService.buscarPorId(id);


        List<ManutencaoDTOResponse> manutencoes = manutencaoService.buscarPorVeiculoId(List.of(id));
            // se não existir método singular, use: manutencaoService.buscarPorVeiculoIdIn(List.of(id));

        Map<String, Object> dados = new HashMap<>();
        dados.put("veiculo", veiculo);
        dados.put("manutencoes", manutencoes);

        byte[] pdfBytes = pdfService.gerarPdf("veiculoDetalhado", dados);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.add(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=relatorio-veiculo-" + id + ".pdf");

        return ResponseEntity.ok()
                .headers(headers)
                .body(pdfBytes);
    }
    @GetMapping("/manutencao/{id}")
    public ResponseEntity<byte[]> gerarRelatorioManutencoesDetalhada(@PathVariable Long id) {

        ManutencaoDTOResponse manutencao = manutencaoService.buscarPorId(id);

        if (manutencao == null) {
            throw new EntityNotFoundException("Manutenção não encontrada para o ID: " + id);

        }

        Map<String, Object> dados = new HashMap<>();
        dados.put("manutencao", manutencao);

        byte[] pdfBytes = pdfService.gerarPdf("manutencaoDetalhada", dados);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.add(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=relatorio-manutencao-" + id + ".pdf");

        return ResponseEntity.ok()
                .headers(headers)
                .body(pdfBytes);
    }
    @GetMapping("/manutencao/listartodas")
    public ResponseEntity<byte[]> gerarRelatorioManutencoesDetalhada() {

        List<ManutencaoDTOResponse> manutencoes = manutencaoService.listarTodas();
        // ou listarTodas(), findAll() — depende do nome real no seu service

        manutencoes.sort((m1, m2) -> {
            if (m1.getDataAgendamento() == null) return 1;
            if (m2.getDataAgendamento() == null) return -1;
            return m2.getDataAgendamento().compareTo(m1.getDataAgendamento());
        });

        Map<String, Object> dados = new HashMap<>();
        dados.put("manutencoes", manutencoes);


        byte[] pdfBytes = pdfService.gerarPdf("listarManutencoes", dados);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.add(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=relatorio-manutencoes.pdf");

        return ResponseEntity.ok()
                .headers(headers)
                .body(pdfBytes);
    }
}
