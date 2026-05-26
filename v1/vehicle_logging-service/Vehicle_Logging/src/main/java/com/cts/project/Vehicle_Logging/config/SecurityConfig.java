package com.cts.project.Vehicle_Logging.config;

import jakarta.servlet.*;
import jakarta.servlet.http.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Configuration
@EnableMethodSecurity
@Slf4j
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/swagger-ui.html",
                                "/swagger-ui/**",
                                "/api-docs",
                                "/api-docs/**",
                                "/v3/api-docs/**",
                                "/logs/internal/**"
                        ).permitAll()
                        .anyRequest().authenticated()
                )
                .addFilterBefore(gatewayHeaderFilter(), UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    private OncePerRequestFilter gatewayHeaderFilter() {
        return new OncePerRequestFilter() {

            @Override
            protected void doFilterInternal(HttpServletRequest req,
                                            HttpServletResponse res,
                                            FilterChain chain)
                    throws ServletException, IOException {

                String email = req.getHeader("X-Auth-Email");
                String role  = req.getHeader("X-Auth-Role");

                if (email != null && role != null) {
                    log.debug("Gateway headers | email={} role={} path={}",
                            email, role, req.getRequestURI());

                    var auth = new UsernamePasswordAuthenticationToken(
                            email, null,
                            List.of(new SimpleGrantedAuthority("ROLE_" + role))
                    );
                    SecurityContextHolder.getContext().setAuthentication(auth);

                } else {
                    log.warn("Gateway headers missing | path={} | "
                                    + "Ensure all requests go through the API Gateway.",
                            req.getRequestURI());
                }

                chain.doFilter(req, res);
            }
        };
    }
}