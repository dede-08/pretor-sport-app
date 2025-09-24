package com.pretor_sport.app.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductoResponseDTO {
    
    private Long id;
    private String nombre;
    private String descripcion;
    private BigDecimal precio;
    private Integer stock;
    private String imagenUrl;
    private CategoriaSimpleDTO categoria;
    private ProveedorSimpleDTO proveedor;
    
    // Campos específicos para artículos deportivos
    private String marca;
    private String modelo;
    private String talla;
    private String color;
    private String genero;
    private String material;
    private BigDecimal peso;
    private List<String> caracteristicas;
    
    // Campos calculados
    private Boolean disponible;
    private String estadoStock;
    private BigDecimal precioConDescuento;
    private Integer descuentoPorcentaje;
    
    //metodo para determinar si el producto está disponible
    public Boolean getDisponible() {
        return stock != null && stock > 0;
    }
    
    //metodo para obtener el estado del stock
    public String getEstadoStock() {
        if (stock == null || stock == 0) {
            return "SIN_STOCK";
        } else if (stock <= 5) {
            return "STOCK_BAJO";
        } else if (stock <= 20) {
            return "STOCK_MEDIO";
        } else {
            return "STOCK_ALTO";
        }
    }
    
    // DTO interno para categoría simplificada
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CategoriaSimpleDTO {
        private Long id;
        private String nombre;
        private String tipo;
        private String iconoUrl;
    }
    
    // DTO interno para proveedor simplificado
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ProveedorSimpleDTO {
        private Long id;
        private String nombre;
        private String email;
    }
}
