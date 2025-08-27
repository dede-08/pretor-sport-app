package com.pretor_sport.app.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "pagos")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Pago {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal monto;

    @Column(name = "fecha_pago", nullable = false)
    private LocalDateTime fechaPago;

    @Column(name = "metodo_pago", nullable = false, length = 50)
    private String metodoPago; // E.g., "TARJETA_CREDITO", "PAYPAL"

    @Column(name = "estado_pago", nullable = false, length = 20)
    private String estado; // E.g., "APROBADO", "RECHAZADO", "PENDIENTE"

    @Column(name = "id_transaccion", length = 100)
    private String idTransaccion; // ID de la pasarela de pago

    @PrePersist
    protected void onCreate() {
        this.fechaPago = LocalDateTime.now();
    }
}

