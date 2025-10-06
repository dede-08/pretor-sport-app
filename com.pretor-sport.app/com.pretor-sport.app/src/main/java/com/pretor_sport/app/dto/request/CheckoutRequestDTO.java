package com.pretor_sport.app.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

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
    private DireccionEnvioDTO direccionEnvio;
    
    @NotNull(message = "La información de pago es obligatoria")
    @Valid
    private InformacionPagoDTO informacionPago;
    
    @Pattern(
        regexp = "^(STANDARD|EXPRESS|PREMIUM)$",
        message = "El tipo de envío debe ser: STANDARD, EXPRESS o PREMIUM"
    )
    private String tipoEnvio = "STANDARD";
    
    @Size(max = 500, message = "Las notas no pueden exceder los 500 caracteres")
    private String notasEspeciales;
    
    //DTO interno para dirección de envío
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DireccionEnvioDTO {
        
        @NotBlank(message = "El nombre completo es obligatorio")
        @Size(max = 200, message = "El nombre no puede exceder los 200 caracteres")
        private String nombreCompleto;
        
        @NotBlank(message = "La dirección es obligatoria")
        @Size(max = 255, message = "La dirección no puede exceder los 255 caracteres")
        private String direccion;
        
        @NotBlank(message = "La ciudad es obligatoria")
        @Size(max = 100, message = "La ciudad no puede exceder los 100 caracteres")
        private String ciudad;
        
        @NotBlank(message = "El estado/provincia es obligatorio")
        @Size(max = 100, message = "El estado no puede exceder los 100 caracteres")
        private String estado;
        
        @NotBlank(message = "El código postal es obligatorio")
        @Pattern(
            regexp = "^\\d{5}(-\\d{4})?$",
            message = "El código postal debe tener el formato: 12345 o 12345-6789"
        )
        private String codigoPostal;
        
        @NotBlank(message = "El país es obligatorio")
        @Size(max = 50, message = "El país no puede exceder los 50 caracteres")
        private String pais;
        
        @Pattern(
            regexp = "^(\\+\\d{1,3}[- ]?)?\\d{10,14}$",
            message = "El formato del teléfono no es válido"
        )
        private String telefono;
    }
    
    // DTO interno para información de pago
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class InformacionPagoDTO {
        
        @NotBlank(message = "El método de pago es obligatorio")
        @Pattern(
            regexp = "^(TARJETA_CREDITO|TARJETA_DEBITO|PAYPAL|TRANSFERENCIA|EFECTIVO_CONTRAENTREGA)$",
            message = "Método de pago inválido"
        )
        private String metodoPago;
        
        // Para pagos con tarjeta
        @Pattern(
            regexp = "^\\d{16}$",
            message = "El número de tarjeta debe tener 16 dígitos"
        )
        private String numeroTarjeta;
        
        @Size(max = 100, message = "El nombre en la tarjeta no puede exceder los 100 caracteres")
        private String nombreTarjeta;
        
        @Pattern(
            regexp = "^(0[1-9]|1[0-2])/\\d{2}$",
            message = "La fecha de vencimiento debe tener el formato MM/YY"
        )
        private String fechaVencimiento;
        
        @Pattern(
            regexp = "^\\d{3,4}$",
            message = "El CVV debe tener 3 o 4 dígitos"
        )
        private String cvv;
        
        // Para otros métodos de pago
        private String emailPaypal;
        private String referenciaPago;
    }
}
