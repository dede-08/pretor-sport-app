package com.pretor_sport.app.dto.request;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CategoriaRequestDTO {
    
    @NotBlank(message = "El nombre de la categoría es obligatorio")
    @Size(min = 3, max = 50, message = "El nombre debe tener entre 3 y 50 caracteres")
    @Pattern(
        regexp = "^[A-ZÁÉÍÓÚÑÜ][a-záéíóúñü\\s]*$",
        message = "El nombre debe comenzar con mayúscula y contener solo letras y espacios"
    )
    private String nombre;
    
    @Size(max = 255, message = "La descripción no puede exceder los 255 caracteres")
    private String descripcion;
    
    @Pattern(
        regexp = "^(https?://).*\\.(jpg|jpeg|png|gif|webp|svg)$",
        message = "La URL del icono debe ser válida y terminar en .jpg, .jpeg, .png, .gif, .webp o .svg"
    )
    private String iconoUrl;
    
    @Pattern(
        regexp = "^(CALZADO|ROPA|EQUIPAMIENTO|ACCESORIOS|SUPLEMENTOS|TECNOLOGIA)$",
        message = "El tipo debe ser: CALZADO, ROPA, EQUIPAMIENTO, ACCESORIOS, SUPLEMENTOS o TECNOLOGIA"
    )
    private String tipo;
    
    @Min(value = 0, message = "El orden no puede ser negativo")
    @Max(value = 999, message = "El orden no puede ser mayor a 999")
    private Integer orden = 0; //para ordenar las categorías en la interfaz
    
    private Boolean activa = true; //para activar/desactivar categorías
}
