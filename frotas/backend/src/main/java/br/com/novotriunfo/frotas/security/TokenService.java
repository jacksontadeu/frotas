package br.com.novotriunfo.frotas.security;

import br.com.novotriunfo.frotas.entity.model.Usuario;
import com.auth0.jwt.JWT;
import org.flywaydb.core.internal.license.FlywayJWTValidationException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import com.auth0.jwt.algorithms.Algorithm;

import java.time.LocalDateTime;
import java.util.Date;

@Service
public class TokenService {

    @Value("${jwt.secret}")
    private String secretKey;



    public String gerarToken(Usuario usuario) {
        try{
            Algorithm algoritmo = Algorithm.HMAC256(secretKey);
            String token = JWT.create()
                    .withIssuer("Frotas")
                    .withSubject(usuario.getEmail())
                    .withClaim("nome", usuario.getNome())
                    .withClaim("id", usuario.getId())
                    .withClaim("role", usuario.getRole().getRole())
                    .withExpiresAt(new Date(System.currentTimeMillis() + 3600000)) // 24 horas
                    .sign(algoritmo);
            return token;
        } catch (FlywayJWTValidationException e) {
            return null;
        }

    }
    public String validarToken(String token) {
        try {
            Algorithm algoritmo = Algorithm.HMAC256(secretKey);
            String email = JWT.require(algoritmo)
                    .withIssuer("Frotas")
                    .build()
                    .verify(token)
                    .getSubject();
            return email;
        } catch (Exception e) {
            return null;
        }
    }


}
