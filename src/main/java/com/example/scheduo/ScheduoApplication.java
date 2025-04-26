package com.example.scheduo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
public class ScheduoApplication {

	public static void main(String[] args) {
		SpringApplication.run(ScheduoApplication.class, args);
	}

}
