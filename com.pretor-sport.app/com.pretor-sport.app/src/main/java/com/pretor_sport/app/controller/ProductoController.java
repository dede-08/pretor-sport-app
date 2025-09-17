package com.pretor_sport.app.controller;

import com.pretor_sport.app.dto.request.ProductoFilterDTO;
import com.pretor_sport.app.dto.request.ProductoRequestDTO;
import com.pretor_sport.app.dto.response.ProductoResponseDTO;
import com.pretor_sport.app.service.ProductoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/productos")
@RequiredArgsConstructor
@Slf4j
public class ProductoController {

    private final ProductoService productoService;

    //lista todos los productos con filtros y paginacion
    @GetMapping
    public ResponseEntity<Page<ProductoResponseDTO>> listarProductos(
            @RequestParam(required = false) String busqueda,
            @RequestParam(required = false) List<Long> categoriaIds,
            @RequestParam(required = false) String marca,
            @RequestParam(required = false) String precioMin,
            @RequestParam(required = false) String precioMax,
            @RequestParam(required = false) List<String> tallas,
            @RequestParam(required = false) List<String> colores,
            @RequestParam(required = false) String genero,
            @RequestParam(required = false) List<String> materiales,
            @RequestParam(defaultValue = "true") Boolean soloDisponibles,
            @RequestParam(required = false) String pesoMin,
            @RequestParam(required = false) String pesoMax,
            @RequestParam(defaultValue = "nombre") String ordenarPor,
            @RequestParam(defaultValue = "asc") String direccion,
            @RequestParam(defaultValue = "0") Integer pagina,
            @RequestParam(defaultValue = "20") Integer tamanoPagina) {
        
        try {
            log.debug("Listando productos con filtros - página: {}, tamaño: {}", pagina, tamanoPagina);
            
            // Crear objeto de filtros
            ProductoFilterDTO filtros = new ProductoFilterDTO();
            filtros.setBusqueda(busqueda);
            filtros.setCategoriaIds(categoriaIds);
            filtros.setMarca(marca);
            filtros.setPrecioMin(precioMin != null ? new java.math.BigDecimal(precioMin) : null);
            filtros.setPrecioMax(precioMax != null ? new java.math.BigDecimal(precioMax) : null);
            filtros.setTallas(tallas);
            filtros.setColores(colores);
            filtros.setGenero(genero);
            filtros.setMateriales(materiales);
            filtros.setSoloDisponibles(soloDisponibles);
            filtros.setPesoMin(pesoMin != null ? new java.math.BigDecimal(pesoMin) : null);
            filtros.setPesoMax(pesoMax != null ? new java.math.BigDecimal(pesoMax) : null);
            filtros.setOrdenarPor(ordenarPor);
            filtros.setDireccion(direccion);
            filtros.setPagina(pagina);
            filtros.setTamanoPagina(tamanoPagina);
            
            Page<ProductoResponseDTO> productos = productoService.listarProductos(filtros);
            
            return ResponseEntity.ok(productos);
            
        } catch (Exception e) {
            log.error("Error al listar productos: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Page.empty());
        }
    }

    /**
     * Obtiene un producto por su ID
     * GET /api/productos/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<?> obtenerProducto(@PathVariable Long id) {
        try {
            log.debug("Obteniendo producto con ID: {}", id);
            
            return productoService.obtenerProductoPorId(id)
                .map(producto -> ResponseEntity.ok(producto))
                .orElse(ResponseEntity.notFound().build());
                
        } catch (Exception e) {
            log.error("Error al obtener producto con ID {}: {}", id, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Error interno", "message", e.getMessage()));
        }
    }

    /**
     * Crea un nuevo producto
     * POST /api/productos
     */
    @PostMapping
    @PreAuthorize("hasAnyRole('EMPLEADO', 'ADMIN')")
    public ResponseEntity<?> crearProducto(@Valid @RequestBody ProductoRequestDTO request) {
        try {
            log.info("Creando nuevo producto: {}", request.getNombre());
            
            ProductoResponseDTO producto = productoService.crearProducto(request);
            
            return ResponseEntity.status(HttpStatus.CREATED).body(producto);
            
        } catch (IllegalArgumentException e) {
            log.warn("Error de validación al crear producto: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("error", "Error de validación", "message", e.getMessage()));
        } catch (Exception e) {
            log.error("Error inesperado al crear producto: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Error interno", "message", "Error inesperado al crear producto"));
        }
    }

    /**
     * Actualiza un producto existente
     * PUT /api/productos/{id}
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('EMPLEADO', 'ADMIN')")
    public ResponseEntity<?> actualizarProducto(
            @PathVariable Long id, 
            @Valid @RequestBody ProductoRequestDTO request) {
        try {
            log.info("Actualizando producto con ID: {}", id);
            
            ProductoResponseDTO producto = productoService.actualizarProducto(id, request);
            
            return ResponseEntity.ok(producto);
            
        } catch (IllegalArgumentException e) {
            log.warn("Error de validación al actualizar producto {}: {}", id, e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("error", "Error de validación", "message", e.getMessage()));
        } catch (Exception e) {
            log.error("Error inesperado al actualizar producto {}: {}", id, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Error interno", "message", "Error inesperado al actualizar producto"));
        }
    }

    /**
     * Elimina un producto (soft delete)
     * DELETE /api/productos/{id}
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> eliminarProducto(@PathVariable Long id) {
        try {
            log.info("Eliminando producto con ID: {}", id);
            
            productoService.eliminarProducto(id);
            
            return ResponseEntity.ok(Map.of("message", "Producto eliminado exitosamente"));
            
        } catch (IllegalArgumentException e) {
            log.warn("Error al eliminar producto {}: {}", id, e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Map.of("error", "Producto no encontrado", "message", e.getMessage()));
        } catch (Exception e) {
            log.error("Error inesperado al eliminar producto {}: {}", id, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Error interno", "message", "Error inesperado al eliminar producto"));
        }
    }

    /**
     * Obtiene productos por categoría
     * GET /api/productos/categoria/{categoriaId}
     */
    @GetMapping("/categoria/{categoriaId}")
    public ResponseEntity<?> obtenerProductosPorCategoria(@PathVariable Long categoriaId) {
        try {
            log.debug("Obteniendo productos para categoría ID: {}", categoriaId);
            
            List<ProductoResponseDTO> productos = productoService.obtenerProductosPorCategoria(categoriaId);
            
            return ResponseEntity.ok(productos);
            
        } catch (Exception e) {
            log.error("Error al obtener productos por categoría {}: {}", categoriaId, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Error interno", "message", e.getMessage()));
        }
    }

    /**
     * Obtiene productos destacados
     * GET /api/productos/destacados
     */
    @GetMapping("/destacados")
    public ResponseEntity<?> obtenerProductosDestacados(
            @RequestParam(defaultValue = "8") Integer limite) {
        try {
            log.debug("Obteniendo {} productos destacados", limite);
            
            List<ProductoResponseDTO> productos = productoService.obtenerProductosDestacados(limite);
            
            return ResponseEntity.ok(productos);
            
        } catch (Exception e) {
            log.error("Error al obtener productos destacados: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Error interno", "message", e.getMessage()));
        }
    }

    /**
     * Busca productos por término de búsqueda
     * GET /api/productos/buscar
     */
    @GetMapping("/buscar")
    public ResponseEntity<?> buscarProductos(
            @RequestParam String termino,
            @RequestParam(defaultValue = "0") Integer pagina,
            @RequestParam(defaultValue = "20") Integer tamanoPagina) {
        try {
            log.debug("Buscando productos con término: {}", termino);
            
            Pageable pageable = PageRequest.of(pagina, tamanoPagina);
            Page<ProductoResponseDTO> productos = productoService.buscarProductos(termino, pageable);
            
            return ResponseEntity.ok(productos);
            
        } catch (Exception e) {
            log.error("Error al buscar productos con término '{}': {}", termino, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Error interno", "message", e.getMessage()));
        }
    }

    /**
     * Obtiene estadísticas de productos (solo para administradores)
     * GET /api/productos/estadisticas
     */
    @GetMapping("/estadisticas")
    @PreAuthorize("hasAnyRole('EMPLEADO', 'ADMIN')")
    public ResponseEntity<?> obtenerEstadisticas() {
        try {
            log.debug("Obteniendo estadísticas de productos");
            
            // Aquí podrías implementar estadísticas como:
            // - Total de productos
            // - Productos por categoría
            // - Productos con stock bajo
            // - Productos más vendidos
            // etc.
            
            Map<String, Object> estadisticas = Map.of(
                "totalProductos", 0, // Implementar en el servicio
                "productosActivos", 0,
                "productosConStockBajo", 0,
                "categoriasConProductos", 0
            );
            
            return ResponseEntity.ok(estadisticas);
            
        } catch (Exception e) {
            log.error("Error al obtener estadísticas de productos: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Error interno", "message", e.getMessage()));
        }
    }

    /**
     * Endpoint de salud para verificar que el servicio de productos está funcionando
     * GET /api/productos/health
     */
    @GetMapping("/health")
    public ResponseEntity<?> health() {
        return ResponseEntity.ok(Map.of(
            "status", "UP",
            "service", "producto-service",
            "timestamp", System.currentTimeMillis()
        ));
    }
}
