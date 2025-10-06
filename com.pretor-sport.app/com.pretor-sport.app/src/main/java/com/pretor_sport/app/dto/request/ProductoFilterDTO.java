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
public class ProductoFilterDTO {
    
    @Size(max = 100, message = "El término de búsqueda no puede exceder los 100 caracteres")
    private String busqueda; //busqueda por nombre o descripción
    
    private List<Long> categoriaIds; //filtrar por categorías
    
    @Size(max = 50, message = "La marca no puede exceder los 50 caracteres")
    private String marca;
    
    @DecimalMin(value = "0.01", message = "El precio mínimo debe ser mayor que 0")
    private BigDecimal precioMin;
    
    @DecimalMax(value = "99999.99", message = "El precio máximo no puede ser mayor a 99,999.99")
    private BigDecimal precioMax;
    
    private List<String> tallas; //XS, S, M, L, XL, etc.
    
    private List<String> colores;
    
    @Pattern(
        regexp = "^(HOMBRE|MUJER|NIÑO|NIÑA|UNISEX)$",
        message = "El género debe ser: HOMBRE, MUJER, NIÑO, NIÑA o UNISEX"
    )
    private String genero;
    
    private List<String> materiales;
    
    private Boolean soloDisponibles = true; //solo mostrar productos en stock
    
    @DecimalMin(value = "0.01", message = "El peso mínimo debe ser mayor que 0")
    private BigDecimal pesoMin;
    
    @DecimalMax(value = "999.99", message = "El peso máximo no puede ser mayor a 999.99")
    private BigDecimal pesoMax;
    
    //parametros de ordenamiento y paginación
    @Pattern(
        regexp = "^(nombre|precio|fechaCreacion|popularidad|descuento)$",
        message = "El campo de ordenamiento debe ser: nombre, precio, fechaCreacion, popularidad o descuento"
    )
    private String ordenarPor = "nombre";
    
    @Pattern(
        regexp = "^(asc|desc)$",
        message = "La dirección debe ser: asc o desc"
    )
    private String direccion = "asc";
    
    @Min(value = 0, message = "La página no puede ser negativa")
    private Integer pagina = 0;
    
    @Min(value = 1, message = "El tamaño de página debe ser al menos 1")
    @Max(value = 100, message = "El tamaño de página no puede ser mayor a 100")
    private Integer tamanoPagina = 20;
    
    //validacion personalizada para el rango de precios
    @AssertTrue(message = "El precio máximo debe ser mayor que el precio mínimo")
    public boolean isPrecioRangeValid() {
        if (precioMin != null && precioMax != null) {
            return precioMax.compareTo(precioMin) >= 0;
        }
        return true;
    }
    
    //validacion personalizada para el rango de pesos
    @AssertTrue(message = "El peso máximo debe ser mayor que el peso mínimo")
    public boolean isPesoRangeValid() {
        if (pesoMin != null && pesoMax != null) {
            return pesoMax.compareTo(pesoMin) >= 0;
        }
        return true;
    }
}
