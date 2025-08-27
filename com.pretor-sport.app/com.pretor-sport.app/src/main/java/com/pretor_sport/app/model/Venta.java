package com.pretor_sport.app.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "ventas")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Venta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pedido_id", nullable = false, unique = true)
    private Pedido pedido;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "pago_id", referencedColumnName = "id")
    private Pago pago;

    @Column(name = "fecha_venta", nullable = false)
    private LocalDateTime fechaVenta;

    @Column(name = "total_venta", nullable = false, precision = 10, scale = 2)
    private BigDecimal totalVenta;

    @PrePersist
    protected void onCreate() {
        this.fechaVenta = LocalDateTime.now();
    }
}
