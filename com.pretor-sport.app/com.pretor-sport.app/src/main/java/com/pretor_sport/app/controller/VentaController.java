package com.pretor_sport.app.controller;

import com.pretor_sport.app.dto.request.CheckoutRequestDTO;
import com.pretor_sport.app.dto.response.VentaResponseDTO;
import com.pretor_sport.app.model.Usuario;
import com.pretor_sport.app.service.AuthService;
import com.pretor_sport.app.service.VentaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/ventas")
@RequiredArgsConstructor
@Slf4j
public class VentaController {

    private final VentaService ventaService;
    private final AuthService authService;

    @PostMapping("/checkout")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<VentaResponseDTO> procesarCheckout(
            @Valid @RequestBody CheckoutRequestDTO request,
            Authentication authentication) {
        Usuario usuario = authService.getCurrentUser(authentication.getName());
        VentaResponseDTO venta = ventaService.procesarCheckout(usuario, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(venta);
    }

    @PostMapping("/checkout/{pedidoId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<VentaResponseDTO> procesarVenta(
            @PathVariable Long pedidoId,
            @RequestParam String metodoPago,
            Authentication authentication) {
        Usuario usuario = authService.getCurrentUser(authentication.getName());
        return ResponseEntity.ok(ventaService.procesarVenta(usuario, pedidoId, metodoPago));
    }

    @GetMapping("/historial")
    @PreAuthorize("hasAnyRole('CLIENTE', 'EMPLEADO', 'ADMIN')")
    public ResponseEntity<Page<VentaResponseDTO>> obtenerHistorial(
            Authentication authentication,
            @RequestParam(defaultValue = "0") Integer pagina,
            @RequestParam(defaultValue = "20") Integer tamanoPagina) {
        Usuario usuario = authService.getCurrentUser(authentication.getName());
        return ResponseEntity.ok(ventaService.obtenerHistorialDeCompras(usuario, PageRequest.of(pagina, Math.min(tamanoPagina, 100))));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('EMPLEADO', 'ADMIN')")
    public ResponseEntity<Page<VentaResponseDTO>> listarTodas(
            @RequestParam(defaultValue = "0") Integer pagina,
            @RequestParam(defaultValue = "20") Integer tamanoPagina) {
        return ResponseEntity.ok(ventaService.listarTodas(PageRequest.of(pagina, Math.min(tamanoPagina, 100))));
    }
}
