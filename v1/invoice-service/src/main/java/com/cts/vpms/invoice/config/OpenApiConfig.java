package com.cts.vpms.invoice.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

// Access the Swagger UI at: http://localhost:8085/swagger-ui.html
// Access the raw JSON spec: http://localhost:8085/v3/api-docs
@Configuration
public class OpenApiConfig {
    @Bean
    public OpenAPI billingServiceOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Billing Service API")
                        .description("Handles dynamic billing, QR payment generation, and invoice management for the Vehicle Parking System.")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("Vehicle Parking Management System Team")
                                .email("dev@vehicleparking.com")
                        )
                );
    }
}
