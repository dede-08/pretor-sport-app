package com.pretor_sport.app.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UsuarioResponseDTO {
    
    private Long id;
    private String nombre;
    private String apellidos;
    private String email;
    private String direccion;
    private String telefono;
    private LocalDateTime fechaRegistro;
    private String rol;
    
    //metodo para obtener el nombre completo
    public String getNombreCompleto() {
        return nombre + " " + apellidos;
    }
    
    //metodo para obtener iniciales
    public String getIniciales() {
        String inicial1 = nombre != null && !nombre.isEmpty() ? nombre.substring(0, 1).toUpperCase() : "";
        String inicial2 = apellidos != null && !apellidos.isEmpty() ? apellidos.substring(0, 1).toUpperCase() : "";
        return inicial1 + inicial2;
    }
}
