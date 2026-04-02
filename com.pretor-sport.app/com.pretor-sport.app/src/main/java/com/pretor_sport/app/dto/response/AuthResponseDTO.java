package com.pretor_sport.app.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
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
    
    @JsonProperty("accessToken")
    private String accessToken;
    
    @JsonProperty("refreshToken")
    private String refreshToken;
    
    @JsonProperty("tokenType")
    private String tokenType = "Bearer";
    
    @JsonProperty("expiresIn")
    private Long expiresIn; //en segundos
    
    @JsonProperty("issuedAt")
    private LocalDateTime issuedAt;
    
    @JsonProperty("expiresAt")
    private LocalDateTime expiresAt;
    
    //info del usuario autenticado
    @JsonProperty("usuario")
    private UsuarioInfo usuario;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UsuarioInfo {
        @JsonProperty("id")
        private Long id;
        
        @JsonProperty("nombre")
        private String nombre;
        
        @JsonProperty("apellidos")
        private String apellidos;
        
        @JsonProperty("email")
        private String email;
        
        @JsonProperty("rol")
        private String rol;
        
        @JsonProperty("nombreCompleto")
        private String nombreCompleto;
        
        @JsonProperty("iniciales")
        private String iniciales;
        
        @JsonProperty("emailVerificado")
        private Boolean emailVerificado;
        
        @JsonProperty("ultimoAcceso")
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
