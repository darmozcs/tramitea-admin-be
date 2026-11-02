package com.darmoz.tramiteaadmin.config;

import com.darmoz.tramiteaadmin.security.DarmozTokenVerifier;
import com.darmoz.tramiteaadmin.security.JwtAuthenticationFilter;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Todo requiere el rol admin de darmoz-auth salvo el login. Sin token (o con
 * uno inválido) 401; con un token válido sin el rol, 403.
 *
 * Sin CORS: Traefik la publica en el mismo dominio que el front del admin
 * (/tramitea-admin-api), y el front la llama desde el servidor.
 */
@Configuration
@EnableConfigurationProperties(DarmozAuthProperties.class)
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http, DarmozTokenVerifier tokenVerifier,
                                           DarmozAuthProperties properties) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/actuator/health", "/actuator/info").permitAll()
                        .requestMatchers("/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html").permitAll()
                        // El rol se chequea adentro (AuthService): sin él, 403.
                        .requestMatchers(HttpMethod.POST, "/auth/login", "/auth/refresh", "/auth/logout").permitAll()
                        // Para que los errores (404, 400...) no se conviertan en 401.
                        .requestMatchers("/error").permitAll()
                        .anyRequest().hasRole(properties.getAdminRole()))
                .addFilterBefore(new JwtAuthenticationFilter(tokenVerifier),
                        UsernamePasswordAuthenticationFilter.class)
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint((request, response, authException) ->
                                response.sendError(HttpServletResponse.SC_UNAUTHORIZED))
                        .accessDeniedHandler((request, response, accessDeniedException) ->
                                response.sendError(HttpServletResponse.SC_FORBIDDEN)))
                .httpBasic(AbstractHttpConfigurer::disable)
                .formLogin(AbstractHttpConfigurer::disable);
        return http.build();
    }
}
