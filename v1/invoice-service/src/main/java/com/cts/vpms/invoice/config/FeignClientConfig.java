package com.cts.vpms.invoice.config;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Configuration
public class FeignClientConfig {

    @Bean
    public RequestInterceptor requestInterceptor() {
        return (RequestTemplate template) -> {
            ServletRequestAttributes attributes =
                    (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attributes != null) {
                HttpServletRequest request = attributes.getRequest();

                String auth = request.getHeader("Authorization");
                if (auth != null && auth.startsWith("Bearer ")) {
                    template.header("Authorization", auth);
                }

                String role = request.getHeader("X-Role");
                if (role != null) {
                    template.header("X-Role", role);
                }

                String email = request.getHeader("X-Auth-Email");
                if (email != null) {
                    template.header("X-Auth-Email", email);
                }
            }
        };
    }
}