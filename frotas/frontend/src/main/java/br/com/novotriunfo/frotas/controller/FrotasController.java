package br.com.novotriunfo.frotas.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class FrotasController {

    @GetMapping("/")
    private String paginaInicial(){
        return "/home";
    }

    @GetMapping("/home")
    public String home(){
        return "/home";
    }
    @GetMapping("/base")
    public String base(){
        return "/cadastrar-base";
    }
    @GetMapping("/veiculo")
    public String veiculo(){
        return "cadastrar-veiculo";
    }
    @GetMapping("/manutencao")
    public String manutencao(){
        return "/cadastrar-manutencao";
    }

}
