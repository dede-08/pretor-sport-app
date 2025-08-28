package com.pretor_sport.app.controller;

import com.pretor_sport.app.dto.request.ClienteRequestDTO;
import com.pretor_sport.app.dto.request.LoginRequestDTO;
import com.pretor_sport.app.dto.request.RefreshTokenRequestDTO;
import com.pretor_sport.app.dto.response.AuthResponseDTO;
import com.pretor_sport.app.model.Cliente;
import com.pretor_sport.app.security.JwtUtil;
import com.pretor_sport.app.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthController {

    private final AuthService authService;
    private final JwtUtil jwtUtil;

    /**
     * Endpoint para iniciar sesión
     */
    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequestDTO loginRequest) {
        try {
            AuthResponseDTO authResponse = authService.login(loginRequest);
            return ResponseEntity.ok(authResponse);
        } catch (Exception e) {
            log.error("Error en login: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(Map.of("error", "Error de autenticación", "message", e.getMessage()));
        }
    }

    /**
     * Endpoint para registrar nuevo usuario
     */
    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody ClienteRequestDTO registroRequest) {
        try {
            AuthResponseDTO authResponse = authService.register(registroRequest);
            return ResponseEntity.status(HttpStatus.CREATED).body(authResponse);
        } catch (IllegalArgumentException e) {
            log.warn("Error en registro: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("error", "Error de registro", "message", e.getMessage()));
        } catch (Exception e) {
            log.error("Error inesperado en registro: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Error interno", "message", "Error inesperado durante el registro"));
        }
    }

    /**
     * Endpoint para renovar access token
     */
    @PostMapping("/refresh")
    public ResponseEntity<?> refreshToken(@Valid @RequestBody RefreshTokenRequestDTO refreshRequest) {
        try {
            AuthResponseDTO authResponse = authService.refreshToken(refreshRequest.getRefreshToken());
            return ResponseEntity.ok(authResponse);
        } catch (IllegalArgumentException e) {
            log.warn("Error al renovar token: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(Map.of("error", "Token inválido", "message", e.getMessage()));
        } catch (Exception e) {
            log.error("Error inesperado al renovar token: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Error interno", "message", "Error al renovar token"));
        }
    }

    /**
     * Endpoint para cerrar sesión
     */
    @PostMapping("/logout")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> logout(HttpServletRequest request) {
        try {
            String authHeader = request.getHeader("Authorization");
            String token = jwtUtil.extractTokenFromHeader(authHeader);
            
            if (token != null) {
                authService.logout(token);
            }
            
            return ResponseEntity.ok(Map.of("message", "Sesión cerrada exitosamente"));
        } catch (Exception e) {
            log.error("Error en logout: {}", e.getMessage());
            return ResponseEntity.ok(Map.of("message", "Sesión cerrada"));
        }
    }

    /**
     * Endpoint para verificar email
     */
    @GetMapping("/verify-email")
    public ResponseEntity<?> verifyEmail(@RequestParam("token") String token) {
        try {
            boolean verified = authService.verificarEmail(token);
            if (verified) {
                return ResponseEntity.ok(Map.of("message", "Email verificado exitosamente"));
            } else {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", "Token inválido", "message", "El token de verificación es inválido o ha expirado"));
            }
        } catch (Exception e) {
            log.error("Error en verificación de email: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Error interno", "message", "Error al verificar email"));
        }
    }

    /**
     * Endpoint para obtener información del usuario actual
     */
    @GetMapping("/me")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> getCurrentUser() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String email = authentication.getName();
            
            Cliente cliente = authService.getCurrentUser(email);
            
            // Crear respuesta sin información sensible
            Map<String, Object> userInfo = Map.of(
                "id", cliente.getId(),
                "nombre", cliente.getNombre(),
                "apellidos", cliente.getApellidos(),
                "email", cliente.getEmail(),
                "direccion", cliente.getDireccion() != null ? cliente.getDireccion() : "",
                "telefono", cliente.getTelefono() != null ? cliente.getTelefono() : "",
                "rol", cliente.getRol().name(),
                "nombreCompleto", cliente.getNombreCompleto(),
                "iniciales", cliente.getIniciales(),
                "emailVerificado", cliente.getEmailVerificado(),
                "fechaRegistro", cliente.getFechaRegistro(),
                "ultimoAcceso", cliente.getUltimoAcceso()
            );
            
            return ResponseEntity.ok(userInfo);
        } catch (Exception e) {
            log.error("Error al obtener información del usuario: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Error interno", "message", "Error al obtener información del usuario"));
        }
    }

    /**
     * Endpoint para validar token
     */
    @PostMapping("/validate-token")
    public ResponseEntity<?> validateToken(HttpServletRequest request) {
        try {
            String authHeader = request.getHeader("Authorization");
            String token = jwtUtil.extractTokenFromHeader(authHeader);
            
            if (token == null) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("valid", false, "message", "Token no proporcionado"));
            }
            
            boolean isValid = jwtUtil.validateTokenStructure(token);
            Map<String, Object> tokenInfo = jwtUtil.getTokenInfo(token);
            
            if (isValid && tokenInfo != null) {
                return ResponseEntity.ok(Map.of(
                    "valid", true,
                    "tokenInfo", tokenInfo
                ));
            } else {
                return ResponseEntity.ok(Map.of(
                    "valid", false,
                    "message", "Token inválido o expirado"
                ));
            }
        } catch (Exception e) {
            log.error("Error al validar token: {}", e.getMessage());
            return ResponseEntity.ok(Map.of(
                "valid", false,
                "message", "Error al validar token"
            ));
        }
    }

    /**
     * Endpoint de salud para verificar que el servicio de auth está funcionando
     */
    @GetMapping("/health")
    public ResponseEntity<?> health() {
        return ResponseEntity.ok(Map.of(
            "status", "UP",
            "service", "auth-service",
            "timestamp", System.currentTimeMillis()
        ));
    }

    /**
     * Endpoint para obtener información sobre los roles disponibles
     */
    @GetMapping("/roles")
    public ResponseEntity<?> getRoles() {
        Map<String, Object> roles = Map.of(
            "ROLE_CLIENTE", Map.of(
                "name", "Cliente",
                "description", "Usuario cliente con permisos básicos"
            ),
            "ROLE_EMPLEADO", Map.of(
                "name", "Empleado", 
                "description", "Empleado con permisos de gestión de productos y pedidos"
            ),
            "ROLE_ADMIN", Map.of(
                "name", "Administrador",
                "description", "Administrador con permisos completos"
            )
        );
        
        return ResponseEntity.ok(roles);
    }
}
