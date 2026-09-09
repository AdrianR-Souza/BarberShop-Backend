package com.github.adrianR_Souza.Barbearia;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@EnableAsync
@SpringBootApplication
public class BarbeariaApplication {

	public static void main(String[] args) {
		SpringApplication.run(BarbeariaApplication.class, args);
	}

}
