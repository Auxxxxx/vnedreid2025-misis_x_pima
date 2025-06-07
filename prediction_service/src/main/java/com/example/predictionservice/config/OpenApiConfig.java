package com.example.predictionservice.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Arrays;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Prediction Service API")
                        .description("Spring Boot API для предсказаний")
                        .version("1.0.0"))
                .servers(Arrays.asList(
                        new Server().url("http://localhost:8080/prediction").description("Local server"),
                        new Server().url("http://109.73.196.162/prediction").description("Remote server")
                ));
    }
} 