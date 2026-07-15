package br.com.novotriunfo.frotas.controller;

import br.com.novotriunfo.frotas.entity.dto.request.LoginDTORequest;
import br.com.novotriunfo.frotas.entity.dto.response.LoginDTOResponse;
import br.com.novotriunfo.frotas.entity.model.Usuario;
import br.com.novotriunfo.frotas.security.TokenService;
import br.com.novotriunfo.frotas.service.LoginService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/login")
@Tag(name="login")
public class LoginController {
    private final AuthenticationManager authenticationManager;
    private final TokenService tokenService;
    private final LoginService loginService;

    public LoginController(AuthenticationManager authenticationManager, TokenService tokenService, LoginService loginService) {
        this.authenticationManager = authenticationManager;
        this.tokenService = tokenService;
        this.loginService = loginService;
    }

    @PostMapping
    @Operation(summary = "Realizar login", description = "Endpoint para realizar login no sistema", security = {})
    public ResponseEntity<LoginDTOResponse> login(@RequestBody LoginDTORequest request) {
        var userPassword = new UsernamePasswordAuthenticationToken(request.email(), request.senha());
        var auth = authenticationManager.authenticate(userPassword);
        var token = tokenService.gerarToken((Usuario) auth.getPrincipal());
        return ResponseEntity.ok(new LoginDTOResponse(token));
    }
}
