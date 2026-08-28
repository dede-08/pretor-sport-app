package com.pretor_sport.app.controller;

import com.pretor_sport.app.dto.request.CarritoItemRequestDTO;
import com.pretor_sport.app.dto.response.PedidoResponseDTO;
import com.pretor_sport.app.model.Usuario;
import com.pretor_sport.app.service.AuthService;
import com.pretor_sport.app.service.PedidoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/carrito")
@RequiredArgsConstructor
@Slf4j
public class CarritoController {

    private final PedidoService pedidoService;
    private final AuthService authService;

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<PedidoResponseDTO> obtenerCarrito(Authentication authentication) {
        Usuario usuario = authService.getCurrentUser(authentication.getName());
        return ResponseEntity.ok(pedidoService.obtenerCarritoActual(usuario));
    }

    @PostMapping("/items")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<PedidoResponseDTO> agregarAlCarrito(
            @Valid @RequestBody CarritoItemRequestDTO request, 
            Authentication authentication) {
        Usuario usuario = authService.getCurrentUser(authentication.getName());
        PedidoResponseDTO pedido = pedidoService.agregarAlCarrito(usuario, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(pedido);
    }

    @PutMapping("/items/{productoId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<PedidoResponseDTO> actualizarCantidad(
            @PathVariable Long productoId,
            @RequestParam int cantidad,
            Authentication authentication) {
        Usuario usuario = authService.getCurrentUser(authentication.getName());
        return ResponseEntity.ok(pedidoService.actualizarCantidad(usuario, productoId, cantidad));
    }

    @DeleteMapping("/items/{productoId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<PedidoResponseDTO> eliminarDelCarrito(
            @PathVariable Long productoId,
            Authentication authentication) {
        Usuario usuario = authService.getCurrentUser(authentication.getName());
        return ResponseEntity.ok(pedidoService.eliminarDelCarrito(usuario, productoId));
    }

    @DeleteMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Map<String, String>> vaciarCarrito(Authentication authentication) {
        Usuario usuario = authService.getCurrentUser(authentication.getName());
        pedidoService.vaciarCarrito(usuario);
        return ResponseEntity.ok(Map.of("message", "Carrito vaciado"));
    }
}
