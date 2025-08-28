package com.pretor_sport.app.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "categorias", indexes = {
    @Index(name = "idx_categoria_tipo", columnList = "tipo"),
    @Index(name = "idx_categoria_activa", columnList = "activa"),
    @Index(name = "idx_categoria_orden", columnList = "orden")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class Categoria {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    @NotBlank(message = "El nombre es obligatorio")
    @Size(min = 3, max = 50, message = "El nombre debe tener entre 3 y 50 caracteres")
    private String nombre;

    @Column(length = 255)
    private String descripcion;

    @Column(name = "icono_url", length = 255)
    private String iconoUrl;

    @Column(length = 20)
    @Enumerated(EnumType.STRING)
    private TipoCategoria tipo;

    @Column(nullable = false)
    @Min(value = 0, message = "El orden no puede ser negativo")
    private Integer orden = 0;

    @Column(nullable = false)
    private Boolean activa = true;

    @CreatedDate
    @Column(name = "fecha_creacion", updatable = false)
    private LocalDateTime fechaCreacion;

    @LastModifiedDate
    @Column(name = "fecha_actualizacion")
    private LocalDateTime fechaActualizacion;

    @OneToMany(mappedBy = "categoria", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Producto> productos;

    // Enum para tipos de categoría
    public enum TipoCategoria {
        CALZADO,
        ROPA,
        EQUIPAMIENTO,
        ACCESORIOS,
        SUPLEMENTOS,
        TECNOLOGIA
    }

    // Método para contar productos activos
    @Transient
    public long getProductosActivosCount() {
        if (productos == null) return 0;
        return productos.stream()
                .filter(p -> p.getActivo() && p.isDisponible())
                .count();
    }
}
