package com.pretor_sport.app.service;

import com.pretor_sport.app.model.*;
import com.pretor_sport.app.repository.*;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.Optional;

@Service
public class PedidoService {

    private final PedidoRepository pedidoRepository;
    private final ProductoRepository productoRepository;

    public PedidoService(PedidoRepository pedidoRepository, ProductoRepository productoRepository) {
        this.pedidoRepository = pedidoRepository;
        this.productoRepository = productoRepository;
    }

    /**
     * Obtiene el carrito de compras activo de un cliente. Si no existe, crea uno nuevo.
     */
    @Transactional
    public Pedido getCarritoActivo(Cliente cliente) {
        return pedidoRepository.findByClienteAndEstado(cliente, "CARRITO")
                .orElseGet(() -> {
                    Pedido nuevoCarrito = new Pedido();
                    nuevoCarrito.setCliente(cliente);
                    nuevoCarrito.setEstado("CARRITO");
                    return pedidoRepository.save(nuevoCarrito);
                });
    }

    /**
     * Agrega un producto al carrito de un cliente.
     * Si el producto ya está en el carrito, actualiza la cantidad.
     */
    @Transactional
    public Pedido agregarProductoAlCarrito(Cliente cliente, Long productoId, int cantidad) {
        Pedido carrito = getCarritoActivo(cliente);
        Producto producto = productoRepository.findById(productoId)
                .orElseThrow(() -> new EntityNotFoundException("Producto no encontrado con id: " + productoId));

        if (producto.getStock() < cantidad) {
            throw new IllegalStateException("Stock insuficiente para el producto: " + producto.getNombre());
        }

        // Busca si el producto ya está en el carrito
        Optional<DetallePedido> detalleExistente = carrito.getDetalles().stream()
                .filter(detalle -> detalle.getProducto().getId().equals(productoId))
                .findFirst();

        if (detalleExistente.isPresent()) {
            // Si ya existe, actualiza la cantidad
            DetallePedido detalle = detalleExistente.get();
            detalle.setCantidad(detalle.getCantidad() + cantidad);
        } else {
            // Si no existe, crea un nuevo detalle
            DetallePedido nuevoDetalle = new DetallePedido();
            nuevoDetalle.setProducto(producto);
            nuevoDetalle.setCantidad(cantidad);
            nuevoDetalle.setPrecioUnitario(producto.getPrecio());
            nuevoDetalle.setPedido(carrito);
            carrito.getDetalles().add(nuevoDetalle);
        }

        return pedidoRepository.save(carrito);
    }
}