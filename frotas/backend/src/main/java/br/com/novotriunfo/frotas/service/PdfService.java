package br.com.novotriunfo.frotas.service;

import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import org.xhtmlrenderer.pdf.ITextRenderer;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.Base64;
import java.util.Map;

@Service
public class PdfService {

    private final TemplateEngine templateEngine;

    public PdfService(TemplateEngine templateEngine) {
        this.templateEngine = templateEngine;
    }


    public byte[] gerarPdf(String templateNome, Map<String, Object> dados) {

        // Injeta a imagem da logo convertida em Base64 nos dados do mapa
        if (dados != null) {
            dados.put("logoBase64", carregarImagemBase64("static/images/logo.png"));
        }

        // 1. Cria o contexto do Thymeleaf e passa as variáveis
        Context context = new Context();
        if (dados != null) {
            context.setVariables(dados);
        }

        // 2. Renderiza o HTML final preenchido com os dados
        String htmlProcessado = templateEngine.process(templateNome, context);

        // 3. Converte a String HTML/XHTML em PDF usando Flying Saucer
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            ITextRenderer renderer = new ITextRenderer();
            renderer.setDocumentFromString(htmlProcessado);
            renderer.layout();
            renderer.createPDF(outputStream);

            return outputStream.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("Erro ao gerar o PDF: " + e.getMessage(), e);
        }
    }


    private String carregarImagemBase64(String caminhoRelativo) {
        try {
            ClassPathResource resource = new ClassPathResource(caminhoRelativo);
            try (InputStream inputStream = resource.getInputStream()) {
                byte[] bytes = inputStream.readAllBytes();
                String base64 = Base64.getEncoder().encodeToString(bytes);
                return "data:image/jpeg;base64," + base64;
            }
        } catch (Exception e) {
            System.err.println("Aviso: Não foi possível carregar a imagem da logo em '" + caminhoRelativo + "': " + e.getMessage());
            return "";
        }
    }
}