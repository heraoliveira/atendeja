package com.hera.atendeja.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    OpenAPI atendejaOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("AtendeJá API")
                        .description("API REST para agenda e fila de atendimento de prestadores locais.")
                        .version("v1")
                        .license(new License().name("Portfolio project")));
    }
}
