package com.github.adrianR_Souza.Barbearia;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

import java.util.TimeZone;

@EnableAsync
@SpringBootApplication
public class BarbeariaApplication {

	public static void main(String[] args) {
		//o servidor (Railway) roda em UTC; sem isso o "agora" fica 3h adiantado
		//e os horarios livres de hoje somem da grade como se ja tivessem passado
		TimeZone.setDefault(TimeZone.getTimeZone("America/Sao_Paulo"));
		SpringApplication.run(BarbeariaApplication.class, args);
	}

}
