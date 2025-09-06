package com.pretor_sport.app.config;

import com.pretor_sport.app.security.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
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

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthFilter;
    private final UserDetailsService userDetailsService;
    private final CorsConfigurationSource corsConfigurationSource;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            // Deshabilitar CSRF ya que usamos JWT
            .csrf(AbstractHttpConfigurer::disable)
            
            // Configurar CORS
            .cors(cors -> cors.configurationSource(corsConfigurationSource))
            
            // Configurar autorización de requests
            .authorizeHttpRequests(auth -> auth
                // Endpoints públicos (sin autenticación)
                // Con context-path /api, las rutas no incluyen el /api
                .requestMatchers("/registro").permitAll()
                .requestMatchers("/auth/register").permitAll()
                .requestMatchers("/auth/login").permitAll()
                .requestMatchers("/auth/refresh").permitAll()
                .requestMatchers("/auth/verify-email").permitAll()
                .requestMatchers("/auth/health").permitAll()
                .requestMatchers("/health").permitAll()
                .requestMatchers("/public/**").permitAll()
                .requestMatchers("/swagger-ui/**", "/v3/api-docs/**").permitAll()
                .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                
                // Endpoints para productos (lectura pública, escritura restringida)
                .requestMatchers(HttpMethod.GET, "/productos/**").permitAll()
                .requestMatchers(HttpMethod.GET, "/categorias/**").permitAll()
                .requestMatchers(HttpMethod.POST, "/productos/**").hasAnyRole("EMPLEADO", "ADMIN")
                .requestMatchers(HttpMethod.PUT, "/productos/**").hasAnyRole("EMPLEADO", "ADMIN")
                .requestMatchers(HttpMethod.DELETE, "/productos/**").hasRole("ADMIN")
                
                // Endpoints para categorías
                .requestMatchers(HttpMethod.POST, "/categorias/**").hasAnyRole("EMPLEADO", "ADMIN")
                .requestMatchers(HttpMethod.PUT, "/categorias/**").hasAnyRole("EMPLEADO", "ADMIN")
                .requestMatchers(HttpMethod.DELETE, "/categorias/**").hasRole("ADMIN")
                
                // Endpoints para proveedores (solo admin y empleados)
                .requestMatchers("/proveedores/**").hasAnyRole("EMPLEADO", "ADMIN")
                
                // Endpoints para clientes
                .requestMatchers(HttpMethod.GET, "/clientes/me").hasAnyRole("CLIENTE", "EMPLEADO", "ADMIN")
                .requestMatchers(HttpMethod.PUT, "/clientes/me").hasAnyRole("CLIENTE", "EMPLEADO", "ADMIN")
                .requestMatchers(HttpMethod.GET, "/clientes/**").hasAnyRole("EMPLEADO", "ADMIN")
                .requestMatchers(HttpMethod.POST, "/clientes/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.PUT, "/clientes/**").hasAnyRole("EMPLEADO", "ADMIN")
                .requestMatchers(HttpMethod.DELETE, "/clientes/**").hasRole("ADMIN")
                
                // Endpoints para pedidos
                .requestMatchers(HttpMethod.GET, "/pedidos/mis-pedidos").hasAnyRole("CLIENTE", "EMPLEADO", "ADMIN")
                .requestMatchers(HttpMethod.POST, "/pedidos").hasAnyRole("CLIENTE", "EMPLEADO", "ADMIN")
                .requestMatchers(HttpMethod.GET, "/pedidos/**").hasAnyRole("EMPLEADO", "ADMIN")
                .requestMatchers(HttpMethod.PUT, "/pedidos/**").hasAnyRole("EMPLEADO", "ADMIN")
                .requestMatchers(HttpMethod.DELETE, "/pedidos/**").hasRole("ADMIN")
                
                // Endpoints para pagos
                .requestMatchers("/pagos/**").hasAnyRole("CLIENTE", "EMPLEADO", "ADMIN")
                
                // Endpoints para ventas (solo empleados y admin)
                .requestMatchers("/ventas/**").hasAnyRole("EMPLEADO", "ADMIN")
                
                // Endpoints de administración (solo admin)
                .requestMatchers("/admin/**").hasRole("ADMIN")
                .requestMatchers("/reportes/**").hasAnyRole("EMPLEADO", "ADMIN")
                .requestMatchers("/estadisticas/**").hasAnyRole("EMPLEADO", "ADMIN")
                
                // Endpoints para carrito de compras
                .requestMatchers("/carrito/**").hasAnyRole("CLIENTE", "EMPLEADO", "ADMIN")
                
                // Cualquier otro endpoint requiere autenticación
                .anyRequest().authenticated()
            )
            
            // Configurar manejo de sesiones como stateless
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            )
            
            // Configurar provider de autenticación
            .authenticationProvider(authenticationProvider())
            
            // Agregar el filtro JWT antes del filtro de autenticación por username/password
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)
            
            // Configurar manejo de excepciones
            .exceptionHandling(exceptions -> exceptions
                .authenticationEntryPoint((request, response, authException) -> {
                    response.setStatus(401);
                    response.setContentType("application/json");
                    response.getWriter().write("{\"error\":\"No autorizado\",\"message\":\"" + authException.getMessage() + "\"}");
                })
                .accessDeniedHandler((request, response, accessDeniedException) -> {
                    response.setStatus(403);
                    response.setContentType("application/json");
                    response.getWriter().write("{\"error\":\"Acceso denegado\",\"message\":\"No tienes permisos para acceder a este recurso\"}");
                })
            );

        return http.build();
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
