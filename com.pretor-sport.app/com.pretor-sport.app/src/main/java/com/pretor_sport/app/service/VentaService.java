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

        CheckoutRequestDTO.DireccionEnvioDatos envio = request.getDireccionEnvio();
        pedido.setNombreEnvio(envio.getNombre());
        pedido.setApellidosEnvio(envio.getApellidos());
        pedido.setDireccionEnvio(envio.getDireccion());
        pedido.setCiudadEnvio(envio.getCiudad());
        pedido.setCodigoPostalEnvio(envio.getCodigoPostal());
        pedido.setTelefonoEnvio(envio.getTelefono());
        pedido.setEmailEnvio(envio.getEmail());
        pedido.setMetodoEnvio(request.getMetodoEnvio().getTipo());
        pedido.setCostoEnvio(request.getMetodoEnvio().getCosto());
        pedido.setNotas(request.getNotas());

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
