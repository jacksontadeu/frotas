package br.com.novotriunfo.frotas.controller;

import br.com.novotriunfo.frotas.client.BaseClient;
import br.com.novotriunfo.frotas.client.VeiculoClient;
import br.com.novotriunfo.frotas.entities.dto.request.VeiculoDTORequest;
import br.com.novotriunfo.frotas.entities.dto.response.BaseDTOEntityResponse;
import br.com.novotriunfo.frotas.entities.dto.response.VeiculoDTOResponse;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.ModelAndView;

import java.util.List;

@Controller
public class VeiculoController {

    private final VeiculoClient veiculoClient;
    private final BaseClient baseClient;

    public VeiculoController(VeiculoClient veiculoClient, BaseClient baseClient) {
        this.veiculoClient = veiculoClient;
        this.baseClient = baseClient;
    }
    @GetMapping("/cadastros/cadastrar-veiculo")
    public ModelAndView cadastrar(){
        VeiculoDTORequest request = new VeiculoDTORequest();
        ModelAndView mv = new ModelAndView("/cadastros/cadastrar-veiculo");
        List<BaseDTOEntityResponse> bases = baseClient.listarTodas();
        mv.addObject("bases", bases);
        mv.addObject("request", request);
        return mv;
    }
    @PostMapping("/cadastrarVeiculo")
    public ModelAndView salvar(VeiculoDTORequest request){
        veiculoClient.cadastrarVeiculo(request);
        return new ModelAndView("redirect:/listagem/listar-veiculos");
    }
    @GetMapping("/listagem/listar-veiculos")
    public ModelAndView listarTodos() {
        ModelAndView mv = new ModelAndView("/listagem/listar-veiculos");
        List<VeiculoDTOResponse> veiculos = veiculoClient.listarTodos();
        mv.addObject("veiculos", veiculos);
        return mv;
    }


}
