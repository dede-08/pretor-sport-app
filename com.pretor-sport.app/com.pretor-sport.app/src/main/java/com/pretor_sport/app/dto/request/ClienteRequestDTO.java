package com.pretor_sport.app.dto.request;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ClienteRequestDTO {
    
    @NotBlank(message = "El nombre es obligatorio")
    @Size(min = 2, max = 100, message = "El nombre debe tener entre 2 y 100 caracteres")
    private String nombre;
    
    @NotBlank(message = "Los apellidos son obligatorios")
    @Size(min = 2, max = 100, message = "Los apellidos deben tener entre 2 y 100 caracteres")
    private String apellidos;
    
    @NotBlank(message = "El email es obligatorio")
    @Email(message = "El formato del email no es válido")
    @Size(max = 100, message = "El email no puede exceder los 100 caracteres")
    private String email;
    
    @NotBlank(message = "La contraseña es obligatoria")
    @Size(min = 8, max = 60, message = "La contraseña debe tener entre 8 y 60 caracteres")
    @Pattern(
        regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&].*$",
        message = "La contraseña debe contener al menos: 1 letra minúscula, 1 mayúscula, 1 número y 1 carácter especial"
    )
    private String password;
    
    @Size(max = 255, message = "La dirección no puede exceder los 255 caracteres")
    private String direccion;
    
    @Pattern(
        regexp = "^(\\+\\d{1,3}[- ]?)?\\d{10,14}$",
        message = "El formato del teléfono no es válido"
    )
    private String telefono;
    
    @Pattern(
        regexp = "^(ROLE_CLIENTE|ROLE_EMPLEADO|ROLE_ADMIN)$",
        message = "El rol debe ser: ROLE_CLIENTE, ROLE_EMPLEADO o ROLE_ADMIN"
    )
    private String rol = "ROLE_CLIENTE";
    
    // Método de utilidad para validar el rol
    @AssertTrue(message = "El rol especificado no es válido")
    public boolean isValidRole() {
        if (rol == null) {
            rol = "ROLE_CLIENTE";
            return true;
        }
        try {
            // Verificar que el rol existe en el enum
            com.pretor_sport.app.model.Cliente.Rol.valueOf(rol);
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }
}
