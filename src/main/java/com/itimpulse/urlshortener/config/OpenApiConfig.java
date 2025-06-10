package com.itimpulse.urlshortener.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import io.swagger.v3.oas.models.servers.Server;
import java.util.List;

@Configuration
public class OpenApiConfig {
    /**
     * Configures OpenAPI documentation for the URL Shortener API.
     * 
     * This configuration sets up the API title, version, contact information,
     * and server details for the OpenAPI documentation.
     * 
     * @return OpenAPI instance with configured metadata
     */ 

    @Value("${openapi.dev-url:http://localhost:8081}")
    private String devUrl;
    @Bean
    public OpenAPI myOpenAPI() {
        Server devServer = new Server();
        devServer.setUrl(devUrl);
        devServer.setDescription("Development server");

        Contact contact = new Contact();
        contact.setName("Onboarding URL Shortener API");

        Info info = new Info()
                .title("Onboarding URL Shortener API")
                .version("1.0")
                .contact(contact)
                .description("A RESTful API for shortening URLs with custom IDs and TTL support.");

        return new OpenAPI()
                .info(info)
                .servers(List.of(devServer));
    }
} 
