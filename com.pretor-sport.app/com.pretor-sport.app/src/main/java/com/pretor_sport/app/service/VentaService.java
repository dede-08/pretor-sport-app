package com.pretor_sport.app.service;

import com.pretor_sport.app.dto.request.CarritoItemRequestDTO;
import com.pretor_sport.app.dto.request.CheckoutRequestDTO;
import com.pretor_sport.app.dto.response.VentaResponseDTO;
import com.pretor_sport.app.mapper.VentaMapper;
import com.pretor_sport.app.model.*;
import com.pretor_sport.app.repository.*;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class VentaService {

    private final VentaRepository ventaRepository;
    private final PedidoRepository pedidoRepository;
    private final ProductoRepository productoRepository;
    private final PagoRepository pagoRepository;
    private final VentaMapper ventaMapper;

    @Transactional
    public VentaResponseDTO procesarCheckout(Usuario usuario, CheckoutRequestDTO request) {
        if (request.getItems().isEmpty()) {
            throw new IllegalStateException("El carrito está vacío");
        }

        List<DetallePedido> detalles = new ArrayList<>();
        BigDecimal subtotal = BigDecimal.ZERO;

        for (CarritoItemRequestDTO item : request.getItems()) {
            Producto producto = productoRepository.findById(item.getProductoId())
                    .orElseThrow(() -> new EntityNotFoundException("Producto no encontrado con ID: " + item.getProductoId()));

            if (!Boolean.TRUE.equals(producto.getActivo())) {
                throw new IllegalStateException("El producto no está disponible: " + producto.getNombre());
            }
            if (producto.getStock() < item.getCantidad()) {
                throw new IllegalStateException("Stock insuficiente para: " + producto.getNombre());
            }

            subtotal = subtotal.add(producto.getPrecio().multiply(BigDecimal.valueOf(item.getCantidad())));

            DetallePedido detalle = new DetallePedido();
            detalle.setProducto(producto);
            detalle.setCantidad(item.getCantidad());
            detalle.setPrecioUnitario(producto.getPrecio());
            detalles.add(detalle);
        }

        BigDecimal total = subtotal.add(request.getMetodoEnvio().getCosto());

        Pedido pedido = new Pedido();
        pedido.setUsuario(usuario);
        pedido.setEstado("COMPLETADO");
        pedido.setDetalles(detalles);
        detalles.forEach(detalle -> detalle.setPedido(pedido));
        Pedido pedidoGuardado = pedidoRepository.save(pedido);

        for (DetallePedido detalle : pedidoGuardado.getDetalles()) {
            Producto producto = detalle.getProducto();
            producto.setStock(producto.getStock() - detalle.getCantidad());
            productoRepository.save(producto);
        }

        Pago pago = new Pago();
        pago.setMonto(total);
        pago.setMetodoPago(request.getMetodoPago().getTipo());
        pago.setEstado("APROBADO");
        pago.setIdTransaccion("TXN-" + System.currentTimeMillis());
        Pago pagoGuardado = pagoRepository.save(pago);

        Venta venta = new Venta();
        venta.setPedido(pedidoGuardado);
        venta.setPago(pagoGuardado);
        venta.setTotalVenta(total);
        Venta ventaGuardada = ventaRepository.save(venta);

        log.info("Compra procesada: usuario={} pedido={} total={} metodoPago={} envio={}",
                usuario.getEmail(), pedidoGuardado.getId(), total,
                request.getMetodoPago().getTipo(), request.getMetodoEnvio().getTipo());

        return ventaMapper.toResponseDTO(ventaGuardada);
    }

    @Transactional
    public VentaResponseDTO procesarVenta(Usuario usuario, Long pedidoId, String metodoPago) {
        Pedido pedido = pedidoRepository.findById(pedidoId)
                .orElseThrow(() -> new EntityNotFoundException("Pedido no encontrado con ID: " + pedidoId));

        if (!pedido.getUsuario().getId().equals(usuario.getId())) {
            log.warn("Intento de procesar pedido ajeno: usuario {} sobre pedido {}", usuario.getId(), pedidoId);
            throw new AccessDeniedException("No puedes procesar un pedido que no te pertenece");
        }

        if (!"CARRITO".equals(pedido.getEstado()) && !"PENDIENTE".equals(pedido.getEstado())) {
            throw new IllegalStateException("El pedido ya ha sido procesado o está en un estado inválido.");
        }
        if (pedido.getDetalles().isEmpty()) {
            throw new IllegalStateException("No se puede procesar un pedido vacío.");
        }

        for (DetallePedido detalle : pedido.getDetalles()) {
            Producto producto = detalle.getProducto();
            if (producto.getStock() < detalle.getCantidad()) {
                throw new IllegalStateException("Stock insuficiente para: " + producto.getNombre());
            }
            producto.setStock(producto.getStock() - detalle.getCantidad());
            productoRepository.save(producto);
        }

        Pago pago = new Pago();
        pago.setMonto(pedido.getSubtotal());
        pago.setMetodoPago(metodoPago);
        pago.setEstado("APROBADO");
        pago.setIdTransaccion("TXN-" + System.currentTimeMillis());
        Pago pagoGuardado = pagoRepository.save(pago);

        Venta venta = new Venta();
        venta.setPedido(pedido);
        venta.setPago(pagoGuardado);
        venta.setTotalVenta(pedido.getSubtotal());
        Venta ventaGuardada = ventaRepository.save(venta);

        pedido.setEstado("COMPLETADO");
        pedidoRepository.save(pedido);

        return ventaMapper.toResponseDTO(ventaGuardada);
    }

    @Transactional(readOnly = true)
    public Page<VentaResponseDTO> obtenerHistorialDeCompras(Usuario usuario, Pageable pageable) {
        Page<Venta> page = ventaRepository.findByPedido_Usuario(usuario, pageable);
        List<VentaResponseDTO> dtos = page.getContent().stream()
                .map(ventaMapper::toResponseDTO)
                .collect(Collectors.toList());
        return new PageImpl<>(dtos, page.getPageable(), page.getTotalElements());
    }

    @Transactional(readOnly = true)
    public Page<VentaResponseDTO> listarTodas(Pageable pageable) {
        Page<Venta> page = ventaRepository.findAll(pageable);
        List<VentaResponseDTO> dtos = page.getContent().stream()
                .map(ventaMapper::toResponseDTO)
                .collect(Collectors.toList());
        return new PageImpl<>(dtos, page.getPageable(), page.getTotalElements());
    }
}
