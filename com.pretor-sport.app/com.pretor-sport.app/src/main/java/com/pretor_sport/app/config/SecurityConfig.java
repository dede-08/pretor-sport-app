package com.pretor_sport.app.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pretor_sport.app.security.JwtAuthenticationFilter;
import com.pretor_sport.app.security.RateLimitFilter;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfigurationSource;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
@RequiredArgsConstructor
@Slf4j
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthFilter;
    private final RateLimitFilter rateLimitFilter;
    private final UserDetailsService userDetailsService;
    private final CorsConfigurationSource corsConfigurationSource;
    private final ObjectMapper objectMapper;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(AbstractHttpConfigurer::disable)
            .cors(cors -> cors.configurationSource(corsConfigurationSource))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/registro").permitAll()
                .requestMatchers("/auth/register").permitAll()
                .requestMatchers("/auth/login").permitAll()
                .requestMatchers("/auth/refresh").permitAll()
                .requestMatchers("/auth/verify-email").permitAll()
                .requestMatchers("/auth/resend-verification").permitAll()
                .requestMatchers("/auth/health").permitAll()
                .requestMatchers("/health").permitAll()
                .requestMatchers("/public/**").permitAll()
                .requestMatchers("/images/**").permitAll()  // Permitir acceso público a imágenes estáticas
                .requestMatchers("/swagger-ui/**", "/v3/api-docs/**").permitAll()
                .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                
                .requestMatchers(HttpMethod.GET, "/productos/**").permitAll()
                .requestMatchers(HttpMethod.GET, "/categorias/**").permitAll()
                .requestMatchers(HttpMethod.POST, "/productos/**").hasAnyRole("EMPLEADO", "ADMIN")
                .requestMatchers(HttpMethod.PUT, "/productos/**").hasAnyRole("EMPLEADO", "ADMIN")
                .requestMatchers(HttpMethod.DELETE, "/productos/**").hasRole("ADMIN")
                
                .requestMatchers(HttpMethod.POST, "/categorias/**").hasAnyRole("EMPLEADO", "ADMIN")
                .requestMatchers(HttpMethod.PUT, "/categorias/**").hasAnyRole("EMPLEADO", "ADMIN")
                .requestMatchers(HttpMethod.DELETE, "/categorias/**").hasRole("ADMIN")
                
                .requestMatchers("/proveedores/**").hasAnyRole("EMPLEADO", "ADMIN")
                
                // Endpoints para usuarios (antes clientes)
                .requestMatchers(HttpMethod.GET, "/usuarios/me").hasAnyRole("CLIENTE", "EMPLEADO", "ADMIN")
                .requestMatchers(HttpMethod.PUT, "/usuarios/me").hasAnyRole("CLIENTE", "EMPLEADO", "ADMIN")
                .requestMatchers(HttpMethod.GET, "/usuarios/**").hasAnyRole("EMPLEADO", "ADMIN")
                .requestMatchers(HttpMethod.POST, "/usuarios/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.PUT, "/usuarios/**").hasAnyRole("EMPLEADO", "ADMIN")
                .requestMatchers(HttpMethod.DELETE, "/usuarios/**").hasRole("ADMIN")
                
                .requestMatchers(HttpMethod.GET, "/pedidos/mis-pedidos").hasAnyRole("CLIENTE", "EMPLEADO", "ADMIN")
                .requestMatchers(HttpMethod.POST, "/pedidos").hasAnyRole("CLIENTE", "EMPLEADO", "ADMIN")
                .requestMatchers(HttpMethod.GET, "/pedidos/**").hasAnyRole("EMPLEADO", "ADMIN")
                .requestMatchers(HttpMethod.PUT, "/pedidos/**").hasAnyRole("EMPLEADO", "ADMIN")
                .requestMatchers(HttpMethod.DELETE, "/pedidos/**").hasRole("ADMIN")
                
                .requestMatchers("/pagos/**").hasAnyRole("CLIENTE", "EMPLEADO", "ADMIN")
                
                .requestMatchers(HttpMethod.POST, "/ventas/checkout").hasAnyRole("CLIENTE", "EMPLEADO", "ADMIN")
                .requestMatchers(HttpMethod.GET, "/ventas/historial").hasAnyRole("CLIENTE", "EMPLEADO", "ADMIN")
                .requestMatchers("/ventas/**").hasAnyRole("EMPLEADO", "ADMIN")
                
                .requestMatchers("/admin/**").hasRole("ADMIN")
                .requestMatchers("/reportes/**").hasAnyRole("EMPLEADO", "ADMIN")
                .requestMatchers("/estadisticas/**").hasAnyRole("EMPLEADO", "ADMIN")

                .anyRequest().authenticated()
            )
            
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            )
            
            .authenticationProvider(authenticationProvider())
            
            .addFilterBefore(rateLimitFilter, UsernamePasswordAuthenticationFilter.class)
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)
            
            .exceptionHandling(exceptions -> exceptions
                .authenticationEntryPoint((request, response, authException) ->
                    writeError(response, HttpStatus.UNAUTHORIZED, "No autenticado",
                        "Debes iniciar sesión para acceder a este recurso", request.getRequestURI()))
                .accessDeniedHandler((request, response, accessDeniedException) ->
                    writeError(response, HttpStatus.FORBIDDEN, "Acceso denegado",
                        "No tienes permisos para acceder a este recurso", request.getRequestURI()))
            );

        return http.build();
    }

    private void writeError(HttpServletResponse response, HttpStatus status,
                            String error, String message, String path) {
        try {
            response.setStatus(status.value());
            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");

            Map<String, Object> body = new LinkedHashMap<>();
            body.put("timestamp", LocalDateTime.now());
            body.put("status", status.value());
            body.put("error", error);
            body.put("message", message);
            body.put("path", path);

            objectMapper.writeValue(response.getWriter(), body);
        } catch (IOException e) {
            log.error("No se pudo escribir la respuesta de error", e);
        }
    }


    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config)
            throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(12);
    }


}

