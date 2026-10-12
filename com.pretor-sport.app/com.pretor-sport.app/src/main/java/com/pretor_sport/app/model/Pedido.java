package com.pretor_sport.app.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.hibernate.annotations.BatchSize;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "pedidos")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Pedido {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @Column(name = "fecha_creacion", nullable = false)
    private LocalDateTime fechaCreacion;

    @Column(nullable = false, length = 20)
    private String estado; //"PENDIENTE", "COMPLETADO"

    @Column(name = "nombre_envio", length = 100)
    private String nombreEnvio;

    @Column(name = "apellidos_envio", length = 100)
    private String apellidosEnvio;

    @Column(name = "direccion_envio", length = 255)
    private String direccionEnvio;

    @Column(name = "ciudad_envio", length = 100)
    private String ciudadEnvio;

    @Column(name = "codigo_postal_envio", length = 10)
    private String codigoPostalEnvio;

    @Column(name = "telefono_envio", length = 20)
    private String telefonoEnvio;

    @Column(name = "email_envio", length = 100)
    private String emailEnvio;

    @Column(name = "metodo_envio", length = 20)
    private String metodoEnvio;

    @Column(name = "costo_envio", precision = 10, scale = 2)
    private BigDecimal costoEnvio;

    @Column(name = "notas", length = 500)
    private String notas;

    @OneToMany(mappedBy = "pedido", cascade = CascadeType.ALL, orphanRemoval = true)
    @BatchSize(size = 25)
    private List<DetallePedido> detalles = new ArrayList<>();

    @PrePersist
    protected void onCreate() {
        this.fechaCreacion = LocalDateTime.now();
    }

    //metodo transitorio para calcular el subtotal del pedido
    @Transient
    public BigDecimal getSubtotal() {
        return detalles.stream()
                .map(DetallePedido::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}