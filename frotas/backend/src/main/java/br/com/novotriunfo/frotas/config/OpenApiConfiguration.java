package br.com.novotriunfo.frotas.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(
        info = @Info(
                title = "Frotas API",
                version = "1.0.0",
                description = "API para gerenciamento de frotas",
                contact = @Contact(
                        name = "Jackson Moraes",
                        url = "https://github.com/jacksontadeu/frotas"

                )

        )

)
public class OpenApiConfiguration {



}
