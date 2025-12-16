package br.com.novotriunfo.frotas;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients
public class FrotasApplication {

	public static void main(String[] args) {
		SpringApplication.run(FrotasApplication.class, args);
	}

}
