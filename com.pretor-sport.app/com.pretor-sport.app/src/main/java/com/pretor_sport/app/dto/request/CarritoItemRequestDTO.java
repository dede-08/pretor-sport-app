package com.pretor_sport.app.dto.request;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CarritoItemRequestDTO {
    
    @NotNull(message = "El ID del producto es obligatorio")
    @Positive(message = "El ID del producto debe ser un número positivo")
    private Long productoId;
    
    @NotNull(message = "La cantidad es obligatoria")
    @Min(value = 1, message = "La cantidad mínima es 1")
    @Max(value = 50, message = "La cantidad máxima es 50 por producto")
    private Integer cantidad;
    
    @Size(max = 50, message = "La talla no puede exceder los 50 caracteres")
    private String tallaSeleccionada;
    
    @Size(max = 30, message = "El color no puede exceder los 30 caracteres")
    private String colorSeleccionado;
    
    @Size(max = 255, message = "Las notas no pueden exceder los 255 caracteres")
    private String notas; // Notas especiales del cliente
}
