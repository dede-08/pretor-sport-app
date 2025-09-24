package com.pretor_sport.app.dto.request;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductoRequestDTO {
    
    @NotBlank(message = "El nombre del producto es obligatorio")
    @Size(min = 3, max = 100, message = "El nombre debe tener entre 3 y 100 caracteres")
    private String nombre;
    
    @Size(max = 1000, message = "La descripción no puede exceder los 1000 caracteres")
    private String descripcion;
    
    @NotNull(message = "El precio es obligatorio")
    @DecimalMin(value = "0.01", message = "El precio debe ser mayor que 0")
    @DecimalMax(value = "99999.99", message = "El precio no puede ser mayor a 99,999.99")
    @Digits(integer = 5, fraction = 2, message = "El precio debe tener máximo 5 dígitos enteros y 2 decimales")
    private BigDecimal precio;
    
    @NotNull(message = "El stock es obligatorio")
    @Min(value = 0, message = "El stock no puede ser negativo")
    @Max(value = 99999, message = "El stock no puede ser mayor a 99,999")
    private Integer stock;
    
    @Pattern(
        regexp = "^(https?://).*\\.(jpg|jpeg|png|gif|webp)$",
        message = "La URL de la imagen debe ser válida y terminar en .jpg, .jpeg, .png, .gif o .webp"
    )
    private String imagenUrl;
    
    //@NotNull(message = "La categoría es obligatoria")
    @Positive(message = "El ID de la categoría debe ser un número positivo")
    private Long categoriaId;
    
    @Positive(message = "El ID del proveedor debe ser un número positivo")
    private Long proveedorId;
    
    // Campos específicos para artículos deportivos
    @Size(max = 50, message = "La marca no puede exceder los 50 caracteres")
    private String marca;
    
    @Size(max = 50, message = "El modelo no puede exceder los 50 caracteres")
    private String modelo;
    
    @Pattern(
        regexp = "^(XS|S|M|L|XL|XXL|XXXL|TALLA_\\d+(\\.\\d)?|UNICO)$",
        message = "La talla debe seguir el formato: XS, S, M, L, XL, XXL, XXXL, TALLA_XX o UNICO"
    )
    private String talla;
    
    @Size(max = 30, message = "El color no puede exceder los 30 caracteres")
    private String color;
    
    @Pattern(
        regexp = "^(HOMBRE|MUJER|NIÑO|NIÑA|UNISEX)$",
        message = "El género debe ser: HOMBRE, MUJER, NIÑO, NIÑA o UNISEX"
    )
    private String genero;
    
    @Size(max = 50, message = "El material no puede exceder los 50 caracteres")
    private String material;
    
    @DecimalMin(value = "0.01", message = "El peso debe ser mayor que 0")
    @DecimalMax(value = "999.99", message = "El peso no puede ser mayor a 999.99 kg")
    private BigDecimal peso; // en kilogramos
    
    private List<String> caracteristicas; // Lista de características adicionales
    
//    @AssertTrue(message = "Debe proporcionarse al menos una imagen")
//    public boolean isImagenUrlValid() {
//        return imagenUrl != null && !imagenUrl.trim().isEmpty();
//    }
}
