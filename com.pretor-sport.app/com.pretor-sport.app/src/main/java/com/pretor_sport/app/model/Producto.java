package com.pretor_sport.app.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "productos", indexes = {
    @Index(name = "idx_producto_categoria", columnList = "categoria_id"),
    @Index(name = "idx_producto_precio", columnList = "precio"),
    @Index(name = "idx_producto_marca", columnList = "marca"),
    @Index(name = "idx_producto_genero", columnList = "genero")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class Producto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    @NotBlank(message = "El nombre es obligatorio")
    private String nombre;

    @Column(length = 1000)
    private String descripcion;

    @Column(nullable = false, precision = 10, scale = 2)
    @NotNull(message = "El precio es obligatorio")
    @DecimalMin(value = "0.01", message = "El precio debe ser mayor que 0")
    private BigDecimal precio;

    @Column(nullable = false)
    @Min(value = 0, message = "El stock no puede ser negativo")
    private int stock;

    @Column(name = "imagen_url", length = 255)
    private String imagenUrl;

    //campos específicos para artículos deportivos
    @Column(length = 50)
    private String marca;

    @Column(length = 50)
    private String modelo;

    @Column(length = 20)
    private String talla;

    @Column(length = 30)
    private String color;

    @Column(length = 10)
    @Enumerated(EnumType.STRING)
    private Genero genero;

    @Column(length = 50)
    private String material;

    @Column(precision = 6, scale = 3)
    private BigDecimal peso; // en kilogramos

    @ElementCollection
    @CollectionTable(name = "producto_caracteristicas", 
                    joinColumns = @JoinColumn(name = "producto_id"))
    @Column(name = "caracteristica")
    private List<String> caracteristicas;

    @Column(name = "activo", nullable = false)
    private Boolean activo = true;

    @CreatedDate
    @Column(name = "fecha_creacion", updatable = false)
    private LocalDateTime fechaCreacion;

    @LastModifiedDate
    @Column(name = "fecha_actualizacion")
    private LocalDateTime fechaActualizacion;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "categoria_id", nullable = false)
    private Categoria categoria;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "proveedor_id")
    private Proveedor proveedor;

    // Enum para género
    public enum Genero {
        HOMBRE, MUJER, NIÑO, NIÑA, UNISEX
    }

    //metodo para verificar disponibilidad
    @Transient
    public boolean isDisponible() {
        return activo && stock > 0;
    }

    //metodo para obtener el estado del stock
    @Transient
    public String getEstadoStock() {
        if (stock == 0) {
            return "SIN_STOCK";
        } else if (stock <= 5) {
            return "STOCK_BAJO";
        } else if (stock <= 20) {
            return "STOCK_MEDIO";
        } else {
            return "STOCK_ALTO";
        }
    }
}
