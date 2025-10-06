package com.pretor_sport.app.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthResponseDTO {
    
    private String accessToken;
    private String refreshToken;
    private String tokenType = "Bearer";
    private Long expiresIn; //en segundos
    private LocalDateTime issuedAt;
    private LocalDateTime expiresAt;
    
    //info del usuario autenticado
    private UsuarioInfo usuario;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UsuarioInfo {
        private Long id;
        private String nombre;
        private String apellidos;
        private String email;
        private String rol;
        private String nombreCompleto;
        private String iniciales;
        private Boolean emailVerificado;
        private LocalDateTime ultimoAcceso;
    }
    
    //metodo de conveniencia para crear una respuesta de autenticación
    public static AuthResponseDTO of(String accessToken, String refreshToken, Long expiresIn, 
                                   Long userId, String nombre, String apellidos, String email, 
                                   String rol, Boolean emailVerificado, LocalDateTime ultimoAcceso) {
        
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime expiresAt = now.plusSeconds(expiresIn);
        
        UsuarioInfo usuarioInfo = UsuarioInfo.builder()
                .id(userId)
                .nombre(nombre)
                .apellidos(apellidos)
                .email(email)
                .rol(rol)
                .nombreCompleto(nombre + " " + apellidos)
                .iniciales(getIniciales(nombre, apellidos))
                .emailVerificado(emailVerificado)
                .ultimoAcceso(ultimoAcceso)
                .build();
        
        return AuthResponseDTO.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .expiresIn(expiresIn)
                .issuedAt(now)
                .expiresAt(expiresAt)
                .usuario(usuarioInfo)
                .build();
    }
    
    private static String getIniciales(String nombre, String apellidos) {
        String inicial1 = nombre != null && !nombre.isEmpty() ? nombre.substring(0, 1).toUpperCase() : "";
        String inicial2 = apellidos != null && !apellidos.isEmpty() ? apellidos.substring(0, 1).toUpperCase() : "";
        return inicial1 + inicial2;
    }
}
