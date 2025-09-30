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
        
        //aplicar filtros y obtener productos
        Page<Producto> productos = productoRepository.findProductosConFiltros(
            filtros.getBusqueda(),
            filtros.getCategoriaIds(),
            filtros.getMarca(),
            filtros.getPrecioMin(),
            filtros.getPrecioMax(),
            filtros.getTallas(),
            filtros.getColores(),
            filtros.getGenero(),
            filtros.getMateriales(),
            filtros.getSoloDisponibles(),
            filtros.getPesoMin(),
            filtros.getPesoMax(),
            pageable
        );
        
        return productos.map(this::convertirADTO);
    }

    //OBTIENE PRODUCTO POR ID
    @Transactional(readOnly = true)
    public Optional<ProductoResponseDTO> obtenerProductoPorId(Long id) {
        log.debug("Obteniendo producto con ID: {}", id);
        return productoRepository.findById(id)
            .map(this::convertirADTO);
    }

    //CREAR UN NUEVO PRODUCTO
    @Transactional
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
        
        return convertirADTO(productoGuardado);
    }

    //ACTUALIZA UN PRODUCTO EXISTENTE
    @Transactional
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
        
        return convertirADTO(productoActualizado);
    }

    //ELIMINA UN PRODUCTO
    @Transactional
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
            .map(this::convertirADTO)
            .collect(Collectors.toList());
    }


    //OBTIENE PRODUCTOS DESTACADOS
    @Transactional(readOnly = true)
    public List<ProductoResponseDTO> obtenerProductosDestacados(int limite) {
        log.debug("Obteniendo {} productos destacados", limite);
        
        return productoRepository.findProductosDestacados(limite)
            .stream()
            .map(this::convertirADTO)
            .collect(Collectors.toList());
    }

    //BUSCA PRODUCTOS POR TERMINO DE BUSQUEDA
    @Transactional(readOnly = true)
    public Page<ProductoResponseDTO> buscarProductos(String termino, Pageable pageable) {
        log.debug("Buscando productos con término: {}", termino);
        
        Page<Producto> productos = productoRepository.findByNombreContainingIgnoreCaseAndActivoTrue(termino, pageable);
        return productos.map(this::convertirADTO);
    }

    //CONVIERTE UN PRODUCTO A ProductoResponseDTO
    private ProductoResponseDTO convertirADTO(Producto producto) {
        ProductoResponseDTO dto = new ProductoResponseDTO();
        dto.setId(producto.getId());
        dto.setNombre(producto.getNombre());
        dto.setDescripcion(producto.getDescripcion());
        dto.setPrecio(producto.getPrecio());
        dto.setStock(producto.getStock());
        dto.setImagenUrl(producto.getImagenUrl());
        dto.setMarca(producto.getMarca());
        dto.setModelo(producto.getModelo());
        dto.setTalla(producto.getTalla());
        dto.setColor(producto.getColor());
        dto.setGenero(producto.getGenero() != null ? producto.getGenero().name() : null);
        dto.setMaterial(producto.getMaterial());
        dto.setPeso(producto.getPeso());
        dto.setCaracteristicas(producto.getCaracteristicas());
        dto.setDisponible(producto.isDisponible());
        dto.setEstadoStock(producto.getEstadoStock());
        
        //convertir categoría
        if (producto.getCategoria() != null) {
            ProductoResponseDTO.CategoriaSimpleDTO categoriaDTO = new ProductoResponseDTO.CategoriaSimpleDTO();
            categoriaDTO.setId(producto.getCategoria().getId());
            categoriaDTO.setNombre(producto.getCategoria().getNombre());
            categoriaDTO.setTipo(producto.getCategoria().getTipo().name());
            categoriaDTO.setIconoUrl(producto.getCategoria().getIconoUrl());
            dto.setCategoria(categoriaDTO);
        }
        
        //convertir proveedor
        if (producto.getProveedor() != null) {
            ProductoResponseDTO.ProveedorSimpleDTO proveedorDTO = new ProductoResponseDTO.ProveedorSimpleDTO();
            proveedorDTO.setId(producto.getProveedor().getId());
            proveedorDTO.setNombre(producto.getProveedor().getNombre());
            proveedorDTO.setEmail(producto.getProveedor().getEmail());
            dto.setProveedor(proveedorDTO);
        }
        
        return dto;
    }
}