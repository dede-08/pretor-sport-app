package com.pretor_sport.app.service;

import com.pretor_sport.app.dto.request.CarritoItemRequestDTO;
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
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class PedidoService {

    private final PedidoRepository pedidoRepository;
    private final ProductoRepository productoRepository;
    private final PedidoMapper pedidoMapper;

    private static final Set<String> ESTADOS_VALIDOS = Set.of("CARRITO", "PENDIENTE", "COMPLETADO");

    @Transactional
    public PedidoResponseDTO obtenerCarritoActual(Usuario usuario) {
        Pedido carrito = getCarritoActivo(usuario);
        return pedidoMapper.toResponseDTO(carrito);
    }

    @Transactional
    public PedidoResponseDTO agregarAlCarrito(Usuario usuario, CarritoItemRequestDTO request) {
        Pedido carrito = getCarritoActivo(usuario);
        Producto producto = productoRepository.findById(request.getProductoId())
                .orElseThrow(() -> new EntityNotFoundException("Producto no encontrado con ID: " + request.getProductoId()));

        if (producto.getStock() < request.getCantidad()) {
            throw new IllegalStateException("Stock insuficiente para el producto: " + producto.getNombre());
        }

        Optional<DetallePedido> detalleExistente = carrito.getDetalles().stream()
                .filter(detalle -> detalle.getProducto().getId().equals(request.getProductoId()))
                .findFirst();

        if (detalleExistente.isPresent()) {
            DetallePedido detalle = detalleExistente.get();
            detalle.setCantidad(detalle.getCantidad() + request.getCantidad());
        } else {
            DetallePedido nuevoDetalle = new DetallePedido();
            nuevoDetalle.setProducto(producto);
            nuevoDetalle.setCantidad(request.getCantidad());
            nuevoDetalle.setPrecioUnitario(producto.getPrecio());
            nuevoDetalle.setPedido(carrito);
            carrito.getDetalles().add(nuevoDetalle);
        }

        Pedido guardado = pedidoRepository.save(carrito);
        return pedidoMapper.toResponseDTO(guardado);
    }

    @Transactional
    public PedidoResponseDTO actualizarCantidad(Usuario usuario, Long productoId, int cantidad) {
        Pedido carrito = getCarritoActivo(usuario);
        DetallePedido detalle = carrito.getDetalles().stream()
                .filter(d -> d.getProducto().getId().equals(productoId))
                .findFirst()
                .orElseThrow(() -> new EntityNotFoundException("Producto no encontrado en el carrito"));

        if (cantidad <= 0) {
            carrito.getDetalles().remove(detalle);
        } else {
            if (detalle.getProducto().getStock() < cantidad) {
                throw new IllegalStateException("Stock insuficiente");
            }
            detalle.setCantidad(cantidad);
        }

        Pedido guardado = pedidoRepository.save(carrito);
        return pedidoMapper.toResponseDTO(guardado);
    }

    @Transactional
    public PedidoResponseDTO eliminarDelCarrito(Usuario usuario, Long productoId) {
        Pedido carrito = getCarritoActivo(usuario);
        carrito.getDetalles().removeIf(d -> d.getProducto().getId().equals(productoId));
        Pedido guardado = pedidoRepository.save(carrito);
        return pedidoMapper.toResponseDTO(guardado);
    }

    @Transactional
    public void vaciarCarrito(Usuario usuario) {
        Pedido carrito = getCarritoActivo(usuario);
        carrito.getDetalles().clear();
        pedidoRepository.save(carrito);
    }

    @Transactional(readOnly = true)
    public Page<PedidoResponseDTO> listarMisPedidos(Usuario usuario, Pageable pageable) {
        Page<Pedido> page = pedidoRepository.findByUsuarioAndEstadoNot(usuario, "CARRITO", pageable);
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

    private Pedido getCarritoActivo(Usuario usuario) {
        return pedidoRepository.findByUsuarioAndEstado(usuario, "CARRITO")
                .orElseGet(() -> {
                    Pedido nuevoCarrito = new Pedido();
                    nuevoCarrito.setUsuario(usuario);
                    nuevoCarrito.setEstado("CARRITO");
                    return pedidoRepository.save(nuevoCarrito);
                });
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
            case "CARRITO" -> destino.equals("PENDIENTE") || destino.equals("COMPLETADO");
            case "PENDIENTE" -> destino.equals("COMPLETADO");
            case "COMPLETADO" -> false;
            default -> false;
        };
    }
}
