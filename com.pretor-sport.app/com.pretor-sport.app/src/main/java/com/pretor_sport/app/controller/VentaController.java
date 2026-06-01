package com.pretor_sport.app.controller;

import com.pretor_sport.app.dto.request.CheckoutRequestDTO;
import com.pretor_sport.app.dto.response.VentaResponseDTO;
import com.pretor_sport.app.model.Usuario;
import com.pretor_sport.app.service.AuthService;
import com.pretor_sport.app.service.VentaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/ventas")
@RequiredArgsConstructor
@Slf4j
public class VentaController {

    private final VentaService ventaService;
    private final AuthService authService;

    @PostMapping("/checkout/{pedidoId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<VentaResponseDTO> procesarVenta(
            @PathVariable Long pedidoId,
            @RequestParam String metodoPago) {
        return ResponseEntity.ok(ventaService.procesarVenta(pedidoId, metodoPago));
    }

    @GetMapping("/historial")
    @PreAuthorize("hasAnyRole('CLIENTE', 'EMPLEADO', 'ADMIN')")
    public ResponseEntity<List<VentaResponseDTO>> obtenerHistorial(Authentication authentication) {
        Usuario usuario = authService.getCurrentUser(authentication.getName());
        return ResponseEntity.ok(ventaService.obtenerHistorialDeCompras(usuario));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('EMPLEADO', 'ADMIN')")
    public ResponseEntity<List<VentaResponseDTO>> listarTodas() {
        return ResponseEntity.ok(ventaService.listarTodas());
    }
}
