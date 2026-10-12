package com.pretor_sport.app.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PedidoResponseDTO {
    private Long id;
    private Long usuarioId;
    private String usuarioNombre;
    private LocalDateTime fechaCreacion;
    private String estado;
    private List<DetallePedidoResponseDTO> detalles;
    private BigDecimal total;

    private String nombreEnvio;
    private String apellidosEnvio;
    private String direccionEnvio;
    private String ciudadEnvio;
    private String codigoPostalEnvio;
    private String telefonoEnvio;
    private String emailEnvio;
    private String metodoEnvio;
    private BigDecimal costoEnvio;
    private String notas;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class DetallePedidoResponseDTO {
        private Long id;
        private Long productoId;
        private String productoNombre;
        private String productoImagenUrl;
        private int cantidad;
        private BigDecimal precioUnitario;
        private BigDecimal subtotal;
    }
}
