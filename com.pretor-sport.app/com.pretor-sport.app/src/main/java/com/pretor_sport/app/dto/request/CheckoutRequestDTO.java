package com.pretor_sport.app.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CheckoutRequestDTO {

    @NotEmpty(message = "Debe incluir al menos un producto")
    @Valid
    private List<CarritoItemRequestDTO> items;

    @NotNull(message = "La información de envío es obligatoria")
    @Valid
    private DireccionEnvioDatos direccionEnvio;

    @NotNull(message = "El método de pago es obligatorio")
    @Valid
    private MetodoPagoDatos metodoPago;

    @NotNull(message = "El método de envío es obligatorio")
    @Valid
    private MetodoEnvioDatos metodoEnvio;

    @Size(max = 500, message = "Las notas no pueden exceder los 500 caracteres")
    private String notas;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DireccionEnvioDatos {

        @NotBlank(message = "El nombre es obligatorio")
        @Size(max = 100, message = "El nombre no puede exceder los 100 caracteres")
        private String nombre;

        @NotBlank(message = "Los apellidos son obligatorios")
        @Size(max = 100, message = "Los apellidos no pueden exceder los 100 caracteres")
        private String apellidos;

        @NotBlank(message = "La dirección es obligatoria")
        @Size(max = 255, message = "La dirección no puede exceder los 255 caracteres")
        private String direccion;

        @NotBlank(message = "La ciudad es obligatoria")
        @Size(max = 100, message = "La ciudad no puede exceder los 100 caracteres")
        private String ciudad;

        @NotBlank(message = "El código postal es obligatorio")
        @Size(max = 10, message = "El código postal no puede exceder los 10 caracteres")
        private String codigoPostal;

        @NotBlank(message = "El teléfono es obligatorio")
        @Size(max = 20, message = "El teléfono no puede exceder los 20 caracteres")
        private String telefono;

        @NotBlank(message = "El email es obligatorio")
        @Email(message = "El formato del email no es válido")
        @Size(max = 100, message = "El email no puede exceder los 100 caracteres")
        private String email;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MetodoPagoDatos {

        @NotBlank(message = "El método de pago es obligatorio")
        @Pattern(
            regexp = "^(TARJETA|PAYPAL|TRANSFERENCIA)$",
            message = "El método de pago debe ser: TARJETA, PAYPAL o TRANSFERENCIA"
        )
        private String tipo;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MetodoEnvioDatos {

        @NotBlank(message = "El tipo de envío es obligatorio")
        @Pattern(
            regexp = "^(ESTANDAR|EXPRESS|PREMIUM)$",
            message = "El tipo de envío debe ser: ESTANDAR, EXPRESS o PREMIUM"
        )
        private String tipo;

        @NotNull(message = "El costo de envío es obligatorio")
        @DecimalMin(value = "0.0", message = "El costo de envío no puede ser negativo")
        @DecimalMax(value = "1000.0", message = "El costo de envío no puede exceder 1000")
        private BigDecimal costo;
    }
}