package com.pretor_sport.app.controller;

import com.pretor_sport.app.dto.response.PedidoResponseDTO;
import com.pretor_sport.app.model.Usuario;
import com.pretor_sport.app.service.AuthService;
import com.pretor_sport.app.service.PedidoService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/pedidos")
@RequiredArgsConstructor
@Slf4j
public class PedidoController {

    private final PedidoService pedidoService;
    private final AuthService authService;

    @GetMapping("/mis-pedidos")
    @PreAuthorize("hasAnyRole('CLIENTE', 'EMPLEADO', 'ADMIN')")
    public ResponseEntity<List<PedidoResponseDTO>> listarMisPedidos(Authentication authentication) {
        Usuario usuario = authService.getCurrentUser(authentication.getName());
        return ResponseEntity.ok(pedidoService.listarMisPedidos(usuario));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('EMPLEADO', 'ADMIN')")
    public ResponseEntity<List<PedidoResponseDTO>> listarTodos() {
        return ResponseEntity.ok(pedidoService.listarTodos());
    }

    @PutMapping("/{id}/estado")
    @PreAuthorize("hasAnyRole('EMPLEADO', 'ADMIN')")
    public ResponseEntity<PedidoResponseDTO> cambiarEstado(
            @PathVariable Long id, 
            @RequestParam String estado) {
        return ResponseEntity.ok(pedidoService.cambiarEstado(id, estado));
    }
}
