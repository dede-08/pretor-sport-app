package com.pretor_sport.app.service;

import com.pretor_sport.app.dto.response.PedidoResponseDTO;
import com.pretor_sport.app.mapper.PedidoMapper;
import com.pretor_sport.app.model.*;
import com.pretor_sport.app.repository.*;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class PedidoService {

    private final PedidoRepository pedidoRepository;
    private final PedidoMapper pedidoMapper;

    private static final Set<String> ESTADOS_VALIDOS = Set.of("PENDIENTE", "COMPLETADO");

    @Transactional(readOnly = true)
    public Page<PedidoResponseDTO> listarMisPedidos(Usuario usuario, Pageable pageable) {
        Page<Pedido> page = pedidoRepository.findByUsuario(usuario, pageable);
        List<PedidoResponseDTO> dtos = page.getContent().stream()
                .map(pedidoMapper::toResponseDTO)
                .collect(Collectors.toList());
        return new PageImpl<>(dtos, page.getPageable(), page.getTotalElements());
    }

    @Transactional(readOnly = true)
    public Page<PedidoResponseDTO> listarTodos(Pageable pageable) {
        Page<Pedido> page = pedidoRepository.findAll(pageable);
        List<PedidoResponseDTO> dtos = page.getContent().stream()
                .map(pedidoMapper::toResponseDTO)
                .collect(Collectors.toList());
        return new PageImpl<>(dtos, page.getPageable(), page.getTotalElements());
    }

    @Transactional
    public PedidoResponseDTO cambiarEstado(Long pedidoId, String nuevoEstado) {
        Pedido pedido = pedidoRepository.findById(pedidoId)
                .orElseThrow(() -> new EntityNotFoundException("Pedido no encontrado"));

        String destino = (nuevoEstado == null ? "" : nuevoEstado.trim().toUpperCase());
        if (!ESTADOS_VALIDOS.contains(destino)) {
            throw new IllegalArgumentException("Estado de pedido inválido: '" + nuevoEstado + "'");
        }
        if (!transicionValida(pedido.getEstado(), destino)) {
            throw new IllegalStateException(
                "No se puede cambiar el pedido del estado '" + pedido.getEstado() + "' a '" + destino + "'");
        }

        pedido.setEstado(destino);
        return pedidoMapper.toResponseDTO(pedidoRepository.save(pedido));
    }

    private boolean transicionValida(String origen, String destino) {
        return switch (origen) {
            case "PENDIENTE" -> destino.equals("COMPLETADO");
            default -> false;
        };
    }
}
