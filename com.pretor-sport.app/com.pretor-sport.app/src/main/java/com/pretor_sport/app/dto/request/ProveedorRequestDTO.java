package com.pretor_sport.app.dto.request;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProveedorRequestDTO {
    
    @NotBlank(message = "El nombre del proveedor es obligatorio")
    @Size(min = 3, max = 100, message = "El nombre debe tener entre 3 y 100 caracteres")
    private String nombre;
    
    @Size(max = 20, message = "El RUC no puede exceder los 20 caracteres")
    private String ruc;
    
    @Size(max = 255, message = "La dirección no puede exceder los 255 caracteres")
    private String direccion;
    
    @Pattern(
        regexp = "^\\+?[0-9\\s-()]{7,20}$",
        message = "El formato del teléfono no es válido"
    )
    private String telefono;
    
    @Email(message = "El formato del email no es válido")
    private String email;
}
