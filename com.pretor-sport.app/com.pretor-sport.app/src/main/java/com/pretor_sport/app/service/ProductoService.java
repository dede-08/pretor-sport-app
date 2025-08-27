package com.pretor_sport.app.service;


import com.pretor_sport.app.model.Producto;
import com.pretor_sport.app.repository.ProductoRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import java.util.Optional;

@Service
public class ProductoService {

    private final ProductoRepository productoRepository;

    public ProductoService(ProductoRepository productoRepository) {
        this.productoRepository = productoRepository;
    }

    //devuelve la pagina de busqueda de productos, opcionalmente filtrada por un termino de busqueda
    public Page<Producto> listarProductosPaginados(Pageable pageable, String busqueda) {
        if (busqueda != null && !busqueda.trim().isEmpty()) {
            return productoRepository.findByNombreContainingIgnoreCase(busqueda, pageable);
        }
        return productoRepository.findAll(pageable);
    }

    public Optional<Producto> obtenerProductoPorId(Long id) {
        return productoRepository.findById(id);
    }

    public Producto guardarProducto(Producto producto) {
        return productoRepository.save(producto);
    }

    public void eliminarProducto(Long id) {
        productoRepository.deleteById(id);
    }
}