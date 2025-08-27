package com.pretor_sport.app.service;

import com.pretor_sport.app.model.*;
import com.pretor_sport.app.repository.*;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.util.List;

@Service
public class VentaService {

    private final VentaRepository ventaRepository;
    private final PedidoRepository pedidoRepository;
    private final ProductoRepository productoRepository;
    private final PagoRepository pagoRepository;
    private final ClienteRepository clienteRepository; // Añadido para buscar el historial

    public VentaService(VentaRepository ventaRepository, PedidoRepository pedidoRepository,
                        ProductoRepository productoRepository, PagoRepository pagoRepository,
                        ClienteRepository clienteRepository) {
        this.ventaRepository = ventaRepository;
        this.pedidoRepository = pedidoRepository;
        this.productoRepository = productoRepository;
        this.pagoRepository = pagoRepository;
        this.clienteRepository = clienteRepository;
    }

    /**
     * Procesa un pedido para generar una venta.
     * Esta operación es transaccional: si algo falla (ej. stock), se revierten todos los cambios.
     * La anotación @Transactional es crucial aquí.
     */
    @Transactional
    public Venta procesarVenta(Long pedidoId, String metodoPago) {
        // 1. Obtener el pedido y validar su estado
        Pedido pedido = pedidoRepository.findById(pedidoId)
                .orElseThrow(() -> new EntityNotFoundException("Pedido no encontrado con id: " + pedidoId));

        if (!"CARRITO".equals(pedido.getEstado())) {
            throw new IllegalStateException("El pedido ya ha sido procesado o está en un estado inválido.");
        }
        if(pedido.getDetalles().isEmpty()) {
            throw new IllegalStateException("No se puede procesar un pedido vacío.");
        }

        // 2. Verificar y actualizar stock de cada producto en el pedido
        for (DetallePedido detalle : pedido.getDetalles()) {
            Producto producto = detalle.getProducto();
            int cantidadSolicitada = detalle.getCantidad();

            if (producto.getStock() < cantidadSolicitada) {
                // Si no hay stock suficiente, se lanza una excepción.
                // Gracias a @Transactional, todas las operaciones anteriores (si las hubiera) se revierten.
                throw new IllegalStateException("Stock insuficiente para el producto: " + producto.getNombre());
            }
            producto.setStock(producto.getStock() - cantidadSolicitada);
            productoRepository.save(producto);
        }

        // 3. Crear el registro de Pago (simulando un pago exitoso)
        Pago pago = new Pago();
        pago.setMonto(pedido.getSubtotal());
        pago.setMetodoPago(metodoPago);
        pago.setEstado("APROBADO");
        pago.setIdTransaccion("TXN-" + System.currentTimeMillis()); // ID de transacción simulado
        Pago pagoGuardado = pagoRepository.save(pago);

        // 4. Crear el registro de Venta, asociando el pedido y el pago
        Venta venta = new Venta();
        venta.setPedido(pedido);
        venta.setPago(pagoGuardado);
        venta.setTotalVenta(pedido.getSubtotal());
        Venta ventaGuardada = ventaRepository.save(venta);

        // 5. Finalmente, actualizar el estado del Pedido a "COMPLETADO"
        pedido.setEstado("COMPLETADO");
        pedidoRepository.save(pedido);

        // Se retorna la venta recién creada
        return ventaGuardada;
    }

    /**
     * Obtiene el historial de compras de un cliente específico.
     */
    public List<Venta> obtenerHistorialDeCompras(Cliente cliente) {
        return ventaRepository.findByPedido_Cliente(cliente);
    }
}
