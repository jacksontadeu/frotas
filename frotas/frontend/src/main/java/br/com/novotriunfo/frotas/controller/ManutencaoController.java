package br.com.novotriunfo.frotas.controller;

import br.com.novotriunfo.frotas.client.ManutencaoClient;
import br.com.novotriunfo.frotas.entities.dto.request.ManutencaoDTORequest;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.ModelAndView;

@Controller
public class ManutencaoController {
    private final ManutencaoClient manutencaoClient;

    public ManutencaoController(ManutencaoClient manutencaoClient) {
        this.manutencaoClient = manutencaoClient;
    }

    @GetMapping("/cadastros/cadastrar-manutencao")
    public ModelAndView cadastrar(){
        ManutencaoDTORequest request = new ManutencaoDTORequest();
        ModelAndView mv = new ModelAndView("/cadastros/cadastrar-manutencao");
        mv.addObject("request", request);
        return mv;
    }
    @PostMapping("/cadastrarManutencao")
    public ModelAndView salvar(ManutencaoDTORequest request){
        manutencaoClient.cadastrarManutencao(request);
        return new ModelAndView("redirect:/listagem/listar-veiculos");
    }
}
