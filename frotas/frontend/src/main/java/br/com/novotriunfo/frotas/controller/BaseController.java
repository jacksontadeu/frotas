package br.com.novotriunfo.frotas.controller;

import br.com.novotriunfo.frotas.client.BaseClient;
import br.com.novotriunfo.frotas.entities.dto.request.BaseDTORequest;
import br.com.novotriunfo.frotas.entities.dto.response.BaseDTOEntityResponse;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.ModelAndView;

import java.util.List;

@Controller
public class BaseController {

    private final BaseClient baseClient;

    public BaseController(BaseClient baseClient) {
        this.baseClient = baseClient;
    }
    @GetMapping("/cadastros/cadastrar-base")
    public ModelAndView cadastrar(){
        BaseDTORequest request = new BaseDTORequest();
        ModelAndView mv = new ModelAndView("/cadastros/cadastrar-base");
        mv.addObject("request", request);
        return mv;
    }
    @PostMapping("/cadastrarBase")
    public ModelAndView salvar(BaseDTORequest request){
        baseClient.cadastrarBase(request);
        return new ModelAndView("redirect:/listagem/listar-bases");
    }
    @GetMapping("/listagem/listar-bases")
    public ModelAndView listarTodos() {
        ModelAndView mv = new ModelAndView("/listagem/listar-bases");
        List<BaseDTOEntityResponse> bases =  baseClient.listarTodas();
        mv.addObject("bases", bases);
        return mv;
    }
}
