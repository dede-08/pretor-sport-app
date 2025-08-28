package com.pretor_sport.app.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final UserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        final String authHeader = request.getHeader("Authorization");
        final String jwt;
        final String userEmail;

        // Si no hay header Authorization o no empieza con Bearer, continúa con la cadena de filtros
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        // Extrae el token JWT del header
        jwt = jwtUtil.extractTokenFromHeader(authHeader);
        
        try {
            // Extrae el email del usuario del token
            userEmail = jwtUtil.extractUsername(jwt);

            // Si tenemos un email y no hay autenticación en el contexto de seguridad
            if (userEmail != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                
                // Carga los detalles del usuario
                UserDetails userDetails = this.userDetailsService.loadUserByUsername(userEmail);

                // Valida el token
                if (jwtUtil.validateToken(jwt, userDetails)) {
                    
                    // Verifica que no sea un refresh token
                    if (!jwtUtil.isRefreshToken(jwt)) {
                        // Crea el token de autenticación
                        UsernamePasswordAuthenticationToken authToken = 
                            new UsernamePasswordAuthenticationToken(
                                userDetails,
                                null,
                                userDetails.getAuthorities()
                            );
                        
                        // Añade detalles adicionales a la autenticación
                        authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                        
                        // Establece la autenticación en el contexto de seguridad
                        SecurityContextHolder.getContext().setAuthentication(authToken);
                        
                        log.debug("Usuario autenticado: {} con rol: {}", 
                            userEmail, userDetails.getAuthorities());
                    } else {
                        log.warn("Intento de usar refresh token como access token para usuario: {}", userEmail);
                    }
                } else {
                    log.warn("Token JWT inválido para usuario: {}", userEmail);
                }
            }
        } catch (Exception e) {
            log.error("Error en la autenticación JWT: {}", e.getMessage());
            // No establecemos la autenticación, dejamos que Spring Security maneje el error
        }

        // Continúa con la cadena de filtros
        filterChain.doFilter(request, response);
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
        String path = request.getRequestURI();
        
        // Lista de endpoints que no necesitan autenticación
        return path.equals("/api/auth/login") ||
               path.equals("/api/auth/register") ||
               path.equals("/api/auth/refresh") ||
               path.equals("/api/health") ||
               path.startsWith("/api/public/") ||
               path.startsWith("/api/v1/api-docs") ||
               path.startsWith("/swagger-ui") ||
               path.startsWith("/v3/api-docs");
    }
}
