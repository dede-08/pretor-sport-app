package com.pretor_sport.app.controller;

import com.pretor_sport.app.dto.request.ProductoFilterDTO;
import com.pretor_sport.app.dto.request.ProductoRequestDTO;
import com.pretor_sport.app.dto.response.ProductoResponseDTO;
import com.pretor_sport.app.service.ProductoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/productos")
@RequiredArgsConstructor
@Slf4j
public class ProductoController {

    @Value("${pagination.max-page-size:100}")
    private int maxPageSize;

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
        
        log.debug("Listando productos con filtros - página: {}, tamaño: {}", pagina, tamanoPagina);
        
        ProductoFilterDTO filtros = new ProductoFilterDTO();
        filtros.setBusqueda(busqueda);
        filtros.setCategoriaIds(categoriaIds);
        filtros.setMarca(marca);
        filtros.setPrecioMin(parseDecimal(precioMin, "precioMin"));
        filtros.setPrecioMax(parseDecimal(precioMax, "precioMax"));
        filtros.setTallas(tallas);
        filtros.setColores(colores);
        filtros.setGenero(genero);
        filtros.setMateriales(materiales);
        filtros.setSoloDisponibles(soloDisponibles);
        filtros.setPesoMin(parseDecimal(pesoMin, "pesoMin"));
        filtros.setPesoMax(parseDecimal(pesoMax, "pesoMax"));
        filtros.setOrdenarPor(ordenarPor);
        filtros.setDireccion(direccion);
        filtros.setPagina(pagina);
        filtros.setTamanoPagina(tamanoPagina);
        
        return ResponseEntity.ok(productoService.listarProductos(filtros));
    }

    //obtiene un producto por su id
    @GetMapping("/{id}")
    public ResponseEntity<ProductoResponseDTO> obtenerProducto(@PathVariable Long id) {
        log.debug("Obteniendo producto con ID: {}", id);
        return productoService.obtenerProductoPorId(id)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }

    //crea un nuevo producto
    @PostMapping("/add")
    @PreAuthorize("hasAnyRole('EMPLEADO', 'ADMIN')")
    public ResponseEntity<ProductoResponseDTO> crearProducto(@Valid @RequestBody ProductoRequestDTO request) {
        log.info("Creando nuevo producto: {}", request.getNombre());
        return ResponseEntity.status(HttpStatus.CREATED).body(productoService.crearProducto(request));
    }

    //actualiza un producto existente
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('EMPLEADO', 'ADMIN')")
    public ResponseEntity<ProductoResponseDTO> actualizarProducto(
            @PathVariable Long id, 
            @Valid @RequestBody ProductoRequestDTO request) {
        log.info("Actualizando producto con ID: {}", id);
        return ResponseEntity.ok(productoService.actualizarProducto(id, request));
    }

    //elimina un producto por id
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, String>> eliminarProducto(@PathVariable Long id) {
        log.info("Eliminando producto con ID: {}", id);
        productoService.eliminarProducto(id);
        return ResponseEntity.ok(Map.of("message", "Producto eliminado exitosamente"));
    }

    //obtiene productos por categoria
    @GetMapping("/categoria/{categoriaId}")
    public ResponseEntity<List<ProductoResponseDTO>> obtenerProductosPorCategoria(@PathVariable Long categoriaId) {
        log.debug("Obteniendo productos para categoría ID: {}", categoriaId);
        return ResponseEntity.ok(productoService.obtenerProductosPorCategoria(categoriaId));
    }

    //obtiene productos destacados
    @GetMapping("/destacados")
    public ResponseEntity<List<ProductoResponseDTO>> obtenerProductosDestacados(
            @RequestParam(defaultValue = "8") Integer limite) {
        log.debug("Obteniendo {} productos destacados", limite);
        return ResponseEntity.ok(productoService.obtenerProductosDestacados(limite));
    }

    //busca productos por termino de busqueda
    @GetMapping("/buscar")
    public ResponseEntity<Page<ProductoResponseDTO>> buscarProductos(
            @RequestParam String termino,
            @RequestParam(defaultValue = "0") Integer pagina,
            @RequestParam(defaultValue = "20") Integer tamanoPagina) {
        log.debug("Buscando productos con término: {}", termino);
        return ResponseEntity.ok(productoService.buscarProductos(termino, PageRequest.of(pagina, Math.min(tamanoPagina, maxPageSize))));
    }

    //obtiene estadisticas de los productos (solo admins)
    @GetMapping("/estadisticas")
    @PreAuthorize("hasAnyRole('EMPLEADO', 'ADMIN')")
    public ResponseEntity<Map<String, Object>> obtenerEstadisticas() {
        log.debug("Obteniendo estadísticas de productos");
        return ResponseEntity.ok(productoService.obtenerEstadisticas());
    }

    @PostMapping("/{id}/image")
    @PreAuthorize("hasAnyRole('EMPLEADO', 'ADMIN')")
    public ResponseEntity<Map<String, String>> uploadProductImage(
            @PathVariable Long id,
            @RequestParam("file") MultipartFile file) throws IOException {
        log.info("Subiendo imagen para producto ID: {}", id);
        String imageUrl = productoService.saveProductImage(id, file);
        return ResponseEntity.ok(Map.of(
            "message", "Imagen subida exitosamente",
            "imagenUrl", imageUrl
        ));
    }



    //endpoint para verificar que el servicio de productos esta funcionando
    @GetMapping("/health")
    public ResponseEntity<?> health() {
        return ResponseEntity.ok(Map.of(
            "status", "UP",
            "service", "producto-service",
            "timestamp", System.currentTimeMillis()
        ));
    }

    //parsea un valor decimal de un filtro; si no es numérico lanza 400 en vez de 500
    private java.math.BigDecimal parseDecimal(String valor, String nombreParametro) {
        if (valor == null || valor.isBlank()) {
            return null;
        }
        try {
            return new java.math.BigDecimal(valor.trim());
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(
                "'" + valor + "' no es un número válido para el filtro '" + nombreParametro + "'");
        }
    }
}
