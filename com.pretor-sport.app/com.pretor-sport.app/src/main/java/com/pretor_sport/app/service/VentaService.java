package com.pretor_sport.app.service;

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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
    public VentaResponseDTO procesarVenta(Long pedidoId, String metodoPago) {
        Pedido pedido = pedidoRepository.findById(pedidoId)
                .orElseThrow(() -> new EntityNotFoundException("Pedido no encontrado con ID: " + pedidoId));

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
