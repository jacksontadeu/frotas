package br.com.novotriunfo.frotas.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@EnableWebSecurity
public class SecurityConfiguration {
    private final SecurityFilter securityFilter;


    public SecurityConfiguration(SecurityFilter securityFilter) {
        this.securityFilter = securityFilter;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(authz -> authz
                        // Login liberado
                        .requestMatchers(HttpMethod.POST, "/login").permitAll()

                        // Usuários: GET liberado; POST, PUT e DELETE exigem ROLE_ADMIN
                        .requestMatchers(HttpMethod.GET, "/usuario", "/usuario/**").permitAll()
                        .requestMatchers(HttpMethod.POST, "/usuario").hasAnyAuthority("ROLE_ADMIN", "ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/usuario/**").hasAnyAuthority("ROLE_ADMIN", "ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/usuario/**").hasAnyAuthority("ROLE_ADMIN", "ADMIN")

                        // Swagger liberado
                        .requestMatchers(
                                "/swagger-ui/**",
                                "/v3/api-docs/**",
                                "/swagger-resources/**",
                                "/webjars/**"
                        ).permitAll()

                        // Rotas de atendimento e manutenção (acessíveis por TÉCNICO e ADMIN)
                        .requestMatchers("/atendimento/**").hasAnyAuthority("ROLE_TECNICO", "ROLE_ADMIN", "TECNICO", "ADMIN")
                        .requestMatchers("/manutencao", "/manutencao/**").hasAnyAuthority("ROLE_TECNICO", "ROLE_ADMIN", "TECNICO", "ADMIN")

                        // Rotas protegidas por ROLE_ADMIN
                        .requestMatchers("/veiculo", "/veiculo/**").hasAnyAuthority("ROLE_ADMIN", "ADMIN")
                        .requestMatchers("/base", "/base/**").hasAnyAuthority("ROLE_ADMIN", "ADMIN")
                        .requestMatchers("/cadastros/**").hasAnyAuthority("ROLE_ADMIN", "ADMIN")

                        // Vue liberado
                        .requestMatchers(
                                "/",
                                "/index.html",
                                "/favicon.ico",
                                "/css/**",
                                "/js/**",
                                "/assets/**",
                                "/images/**"
                        ).permitAll()

                        // Qualquer outra requisição precisa estar autenticada
                        .anyRequest().authenticated()
                )
                .addFilterBefore(securityFilter, UsernamePasswordAuthenticationFilter.class)
                .build();
    }



    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

}
