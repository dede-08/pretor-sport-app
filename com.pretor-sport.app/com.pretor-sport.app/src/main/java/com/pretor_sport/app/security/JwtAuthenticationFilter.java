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

        //si no hay header Authorization o no empieza con Bearer, continua con la cadena de filtros
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        //extrae el token JWT del header
        jwt = jwtUtil.extractTokenFromHeader(authHeader);
        
        try {
            //extrae el email del usuario del token
            userEmail = jwtUtil.extractUsername(jwt);

            //si tenemos un email y no hay autenticacion en el contexto de seguridad
            if (userEmail != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                
                //carga los detalles del usuario
                UserDetails userDetails = this.userDetailsService.loadUserByUsername(userEmail);

                //valida el token
                if (jwtUtil.validateToken(jwt, userDetails)) {
                    
                    //verifica que no sea un refresh token
                    if (!jwtUtil.isRefreshToken(jwt)) {
                        //crea el token de autenticacion
                        UsernamePasswordAuthenticationToken authToken = 
                            new UsernamePasswordAuthenticationToken(
                                userDetails,
                                null,
                                userDetails.getAuthorities()
                            );
                        
                        //añade detalles adicionales a la autenticacion
                        authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                        
                        //establece la autenticacion en el contexto de seguridad
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
            // No establecemos la autenticacion, dejamos que spring security maneje el error
        }

        //continua con la cadena de filtros
        filterChain.doFilter(request, response);
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request){
        String path = request.getServletPath();
        System.out.println("SERVLET PATH = " + path);
        
        //lista de endpoints que no necesitan autenticación
        return path.startsWith("/auth/register") ||
                path.startsWith("/auth/login") ||
                path.startsWith("/auth/refresh") ||
                path.startsWith("/auth/verify-email") ||
                path.startsWith("/auth/health") ||
                path.startsWith("/health") ||
                path.startsWith("/public/") ||
                path.startsWith("/v1/api-docs") ||
                path.startsWith("/swagger-ui") ||
                path.startsWith("/v3/api-docs");
    }
}
