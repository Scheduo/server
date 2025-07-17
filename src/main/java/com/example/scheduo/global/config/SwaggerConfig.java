package com.example.scheduo.global.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;

@Configuration
public class SwaggerConfig {
	@Bean
	public OpenAPI openAPI() {

		SecurityScheme securityScheme = new SecurityScheme()
			.type(SecurityScheme.Type.HTTP)
			.scheme("bearer")
			.bearerFormat("JWT")
			.in(SecurityScheme.In.HEADER)
			.name("JWT Authorization");

		SecurityRequirement securityRequirement = new SecurityRequirement().addList("BearerAuth");

		return new OpenAPI()
			.info(new Info()
				.title("Scheduo 프로젝트 API 명세서")
				.description("Scheduo 프로젝트의 Swagger 기반 API 문서입니다.")
				.version("v1.0.0")
			)
			.addSecurityItem(securityRequirement)
			.schemaRequirement("BearerAuth", securityScheme)
			.addServersItem(new Server().url("/"));
	}
}