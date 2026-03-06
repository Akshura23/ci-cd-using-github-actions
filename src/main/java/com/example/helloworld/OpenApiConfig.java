package com.example.helloworld;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI helloWorldOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Hello World Log Comparison API")
                        .description("REST API for uploading and comparing log files")
                        .version("v1")
                        .contact(new Contact().name("API Support"))
                        .license(new License().name("MIT")));
    }
}

