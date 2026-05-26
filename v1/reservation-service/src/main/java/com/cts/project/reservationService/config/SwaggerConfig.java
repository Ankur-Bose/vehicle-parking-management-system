    package com.cts.project.reservationService.config;

    import io.swagger.v3.oas.models.OpenAPI;
    import io.swagger.v3.oas.models.info.Contact;
    import io.swagger.v3.oas.models.info.Info;
    import org.springframework.context.annotation.Bean;
    import org.springframework.context.annotation.Configuration;

    @Configuration
    public class SwaggerConfig {

        @Bean
        public OpenAPI customOpenAPI() {
            return new OpenAPI()
                    .info(new Info()
                            .title("Vehicle Parking Management System")
                            .version("1.0.0")
                            .description(
                                    "REST API for managing vehicle parking reservations. " +
                                            "Supports creating, viewing, updating and cancelling reservations. " +
                                            "Slot availability is automatically managed based on reservation status."
                            )
                            .contact(new Contact()
                                    .name("Lokesh Gandham")
                                    .email("lokesh.gandham@cognizant.com")
                            )
                    );
        }
    }