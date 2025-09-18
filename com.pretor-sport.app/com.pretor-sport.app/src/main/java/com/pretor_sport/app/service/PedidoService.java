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


    //obtiene el carrito de compras activo de un cliente. si no existe
    @Transactional
    public Pedido getCarritoActivo(Usuario usuario) {
        return pedidoRepository.findByUsuarioAndEstado(usuario, "CARRITO")
                .orElseGet(() -> {
                    Pedido nuevoCarrito = new Pedido();
                    nuevoCarrito.setUsuario(usuario);
                    nuevoCarrito.setEstado("CARRITO");
                    return pedidoRepository.save(nuevoCarrito);
                });
    }

    //agrega un producto al carrito del cliente, si el producto ya esta en el carrito se actualiza la cantidad
    @Transactional
    public Pedido agregarProductoAlCarrito(Usuario usuario, Long productoId, int cantidad) {
        Pedido carrito = getCarritoActivo(usuario);
        Producto producto = productoRepository.findById(productoId)
                .orElseThrow(() -> new EntityNotFoundException("Producto no encontrado con id: " + productoId));

        if (producto.getStock() < cantidad) {
            throw new IllegalStateException("Stock insuficiente para el producto: " + producto.getNombre());
        }

        //busca si el producto ya está en el carrito
        Optional<DetallePedido> detalleExistente = carrito.getDetalles().stream()
                .filter(detalle -> detalle.getProducto().getId().equals(productoId))
                .findFirst();

        if (detalleExistente.isPresent()) {
            //si ya existe, actualiza la cantidad
            DetallePedido detalle = detalleExistente.get();
            detalle.setCantidad(detalle.getCantidad() + cantidad);
        } else {
            //si no existe, crea un nuevo detalle
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