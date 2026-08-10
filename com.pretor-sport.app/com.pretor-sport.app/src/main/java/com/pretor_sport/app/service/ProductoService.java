package com.pretor_sport.app.service;

import com.pretor_sport.app.dto.request.ProductoFilterDTO;
import com.pretor_sport.app.dto.request.ProductoRequestDTO;
import com.pretor_sport.app.dto.response.ProductoResponseDTO;
import com.pretor_sport.app.model.Categoria;
import com.pretor_sport.app.model.Producto;
import com.pretor_sport.app.model.Proveedor;
import com.pretor_sport.app.repository.CategoriaRepository;
import com.pretor_sport.app.repository.ProductoRepository;
import com.pretor_sport.app.repository.ProveedorRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProductoService {

    private final ProductoRepository productoRepository;
    private final CategoriaRepository categoriaRepository;
    private final ProveedorRepository proveedorRepository;
    private final FileStorageService fileStorageService;
    private final com.pretor_sport.app.mapper.ProductoMapper productoMapper;

    //LISTA TODOS LOS PRODUCTOS CON PAGINACION Y FILTROS
    @Transactional(readOnly = true)
    public Page<ProductoResponseDTO> listarProductos(ProductoFilterDTO filtros) {
        log.debug("Listando productos con filtros: {}", filtros);

        //validar y crear objeto de paginación
        String ordenarPor = filtros.getOrdenarPor();
        List<String> camposValidos = List.of("nombre", "precio", "marca", "modelo", "fechaCreacion", "stock", "genero", "peso");
        if (ordenarPor == null || !camposValidos.contains(ordenarPor)) {
            ordenarPor = "nombre"; //campo por defecto
        }

        Sort sort = Sort.by(
            filtros.getDireccion().equalsIgnoreCase("desc") ?
                Sort.Direction.DESC : Sort.Direction.ASC,
            ordenarPor
        );

        Pageable pageable = PageRequest.of(
            filtros.getPagina(), 
            filtros.getTamanoPagina(), 
            sort
        );

        //preparar parametros para LIKE
        String busquedaLike = null;
        if (filtros.getBusqueda() != null && !filtros.getBusqueda().isEmpty()) {
            busquedaLike = "%" + filtros.getBusqueda() + "%";
        }

        String marcaLike = null;
        if (filtros.getMarca() != null && !filtros.getMarca().isEmpty()) {
            marcaLike = "%" + filtros.getMarca() + "%";
        }

        //convertir genero a Enum
        Producto.Genero generoEnum = null;
        if (filtros.getGenero() != null) {
            try {
                generoEnum = Producto.Genero.valueOf(filtros.getGenero().toUpperCase());
            } catch (IllegalArgumentException e) {
                log.warn("Valor de genero no válido: {}", filtros.getGenero());
            }
        }

        //aplicar filtros y obtener productos
        Page<Producto> productos = productoRepository.findProductosConFiltros(
            busquedaLike,
            filtros.getCategoriaIds(),
            marcaLike,
            filtros.getPrecioMin(),
            filtros.getPrecioMax(),
            filtros.getTallas(),
            filtros.getColores(),
            generoEnum,
            filtros.getMateriales(),
            filtros.getSoloDisponibles(),
            filtros.getPesoMin(),
            filtros.getPesoMax(),
            pageable
        );

        return productos.map(productoMapper::toResponseDTO);
    }

    //OBTIENE PRODUCTO POR ID
    @Transactional(readOnly = true)
    @org.springframework.cache.annotation.Cacheable(value = "productos", key = "#id")
    public Optional<ProductoResponseDTO> obtenerProductoPorId(Long id) {
        log.debug("Obteniendo producto con ID: {}", id);
        return productoRepository.findById(id)
            .map(productoMapper::toResponseDTO);
    }

    //CREAR UN NUEVO PRODUCTO
    @Transactional
    @org.springframework.cache.annotation.CacheEvict(value = "productos", allEntries = true)
    public ProductoResponseDTO crearProducto(ProductoRequestDTO request) {
        log.info("Creando nuevo producto: {}", request.getNombre());

        //validar que la categoría existe
        Categoria categoria = categoriaRepository.findById(request.getCategoriaId())
            .orElseThrow(() -> new IllegalArgumentException("Categoría no encontrada con ID: " + request.getCategoriaId()));

        //validar que el proveedor existe (si se proporciona)
        Proveedor proveedor = null;
        if (request.getProveedorId() != null) {
            proveedor = proveedorRepository.findById(request.getProveedorId())
                .orElseThrow(() -> new IllegalArgumentException("Proveedor no encontrado con ID: " + request.getProveedorId()));
        }

        //crear el producto
        Producto producto = new Producto();
        producto.setNombre(request.getNombre());
        producto.setDescripcion(request.getDescripcion());
        producto.setPrecio(request.getPrecio());
        producto.setStock(request.getStock());
        producto.setImagenUrl(request.getImagenUrl());
        producto.setCategoria(categoria);
        producto.setProveedor(proveedor);
        producto.setMarca(request.getMarca());
        producto.setModelo(request.getModelo());
        producto.setTalla(request.getTalla());
        producto.setColor(request.getColor());
        producto.setGenero(request.getGenero() != null ? 
            Producto.Genero.valueOf(request.getGenero()) : null);
        producto.setMaterial(request.getMaterial());
        producto.setPeso(request.getPeso());
        producto.setCaracteristicas(request.getCaracteristicas());
        producto.setActivo(true);

        Producto productoGuardado = productoRepository.save(producto);
        log.info("Producto creado exitosamente con ID: {}", productoGuardado.getId());

        return productoMapper.toResponseDTO(productoGuardado);
    }

    //ACTUALIZA UN PRODUCTO EXISTENTE
    @Transactional
    @org.springframework.cache.annotation.CacheEvict(value = "productos", allEntries = true)
    public ProductoResponseDTO actualizarProducto(Long id, ProductoRequestDTO request) {
        log.info("Actualizando producto con ID: {}", id);

        Producto producto = productoRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Producto no encontrado con ID: " + id));

        //validar que la categoría existe
        Categoria categoria = categoriaRepository.findById(request.getCategoriaId())
            .orElseThrow(() -> new IllegalArgumentException("Categoría no encontrada con ID: " + request.getCategoriaId()));

        //validar que el proveedor existe (si se proporciona)
        Proveedor proveedor = null;
        if (request.getProveedorId() != null) {
            proveedor = proveedorRepository.findById(request.getProveedorId())
                .orElseThrow(() -> new IllegalArgumentException("Proveedor no encontrado con ID: " + request.getProveedorId()));
        }

        //actualizar campos
        producto.setNombre(request.getNombre());
        producto.setDescripcion(request.getDescripcion());
        producto.setPrecio(request.getPrecio());
        producto.setStock(request.getStock());
        producto.setImagenUrl(request.getImagenUrl());
        producto.setCategoria(categoria);
        producto.setProveedor(proveedor);
        producto.setMarca(request.getMarca());
        producto.setModelo(request.getModelo());
        producto.setTalla(request.getTalla());
        producto.setColor(request.getColor());
        producto.setGenero(request.getGenero() != null ? 
            Producto.Genero.valueOf(request.getGenero()) : null);
        producto.setMaterial(request.getMaterial());
        producto.setPeso(request.getPeso());
        producto.setCaracteristicas(request.getCaracteristicas());

        Producto productoActualizado = productoRepository.save(producto);
        log.info("Producto actualizado exitosamente con ID: {}", productoActualizado.getId());

        return productoMapper.toResponseDTO(productoActualizado);
    }

    //ELIMINA UN PRODUCTO
    @Transactional
    @org.springframework.cache.annotation.CacheEvict(value = "productos", allEntries = true)
    public void eliminarProducto(Long id) {
        log.info("Eliminando producto con ID: {}", id);

        Producto producto = productoRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Producto no encontrado con ID: " + id));

        producto.setActivo(false);
        productoRepository.save(producto);

        log.info("Producto eliminado exitosamente con ID: {}", id);
    }

    //OBTIENE PRODUCTOS POR CATEGORIA
    @Transactional(readOnly = true)
    public List<ProductoResponseDTO> obtenerProductosPorCategoria(Long categoriaId) {
        log.debug("Obteniendo productos para categoría ID: {}", categoriaId);

        return productoRepository.findByCategoriaIdAndActivoTrue(categoriaId)
            .stream()
            .map(productoMapper::toResponseDTO)
            .collect(Collectors.toList());
    }


    //OBTIENE PRODUCTOS DESTACADOS
    @Transactional(readOnly = true)
    @org.springframework.cache.annotation.Cacheable(value = "productos", key = "'destacados_' + #limite")
    public List<ProductoResponseDTO> obtenerProductosDestacados(int limite) {
        log.debug("Obteniendo {} productos destacados", limite);

        return productoRepository.findProductosDestacados(limite)
            .stream()
            .map(productoMapper::toResponseDTO)
            .collect(Collectors.toList());
    }

    //BUSCA PRODUCTOS POR TERMINO DE BUSQUEDA
    @Transactional(readOnly = true)
    public Page<ProductoResponseDTO> buscarProductos(String termino, Pageable pageable) {
        log.debug("Buscando productos con término: {}", termino);

        Page<Producto> productos = productoRepository.findByNombreContainingIgnoreCaseAndActivoTrue(termino, pageable);
        return productos.map(productoMapper::toResponseDTO);
    }

    @Transactional
    public String saveProductImage(Long productoId, MultipartFile file) throws IOException {
        Producto producto = productoRepository.findById(productoId)
                .orElseThrow(() -> new IllegalArgumentException("Producto no encontrado con ID: " + productoId));

        String imageUrl = fileStorageService.storeFile(file);

        producto.setImagenUrl(imageUrl);
        productoRepository.save(producto);

        log.info("Imagen guardada exitosamente para el producto {}: {}", productoId, imageUrl);
        return imageUrl;
    }
}