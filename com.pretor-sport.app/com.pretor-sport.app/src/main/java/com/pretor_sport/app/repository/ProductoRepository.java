package com.pretor_sport.app.repository;

import com.pretor_sport.app.model.Producto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

@Repository
public interface ProductoRepository extends JpaRepository<Producto, Long> {
    
    // Busca productos cuyo nombre contenga el término de búsqueda, ignorando mayúsculas y minúsculas
    Page<Producto> findByNombreContainingIgnoreCase(String nombre, Pageable pageable);
    
    // Busca productos activos cuyo nombre contenga el término de búsqueda
    Page<Producto> findByNombreContainingIgnoreCaseAndActivoTrue(String nombre, Pageable pageable);
    
    // Obtiene productos por categoría y que estén activos
    List<Producto> findByCategoriaIdAndActivoTrue(Long categoriaId);
    
    // Obtiene productos destacados (puedes personalizar la lógica)
    @Query("SELECT p FROM Producto p WHERE p.activo = true ORDER BY p.fechaCreacion DESC")
    List<Producto> findProductosDestacados(Pageable pageable);
    
    //metodo personalizado para filtrar productos con múltiples criterios
    @Query("SELECT p FROM Producto p WHERE " +
           "(:busqueda IS NULL OR LOWER(p.nombre) LIKE LOWER(CONCAT('%', :busqueda, '%')) OR " +
           "LOWER(p.descripcion) LIKE LOWER(CONCAT('%', :busqueda, '%'))) AND " +
           "(:categoriaIds IS NULL OR p.categoria.id IN :categoriaIds) AND " +
           "(:marca IS NULL OR LOWER(p.marca) LIKE LOWER(CONCAT('%', :marca, '%'))) AND " +
           "(:precioMin IS NULL OR p.precio >= :precioMin) AND " +
           "(:precioMax IS NULL OR p.precio <= :precioMax) AND " +
           "(:tallas IS NULL OR p.talla IN :tallas) AND " +
           "(:colores IS NULL OR p.color IN :colores) AND " +
           "(:genero IS NULL OR p.genero = :genero) AND " +
           "(:materiales IS NULL OR LOWER(p.material) IN :materiales) AND " +
           "(:soloDisponibles = false OR (p.activo = true AND p.stock > 0)) AND " +
           "(:pesoMin IS NULL OR p.peso >= :pesoMin) AND " +
           "(:pesoMax IS NULL OR p.peso <= :pesoMax)")
    Page<Producto> findProductosConFiltros(
            @Param("busqueda") String busqueda,
            @Param("categoriaIds") List<Long> categoriaIds,
            @Param("marca") String marca,
            @Param("precioMin") BigDecimal precioMin,
            @Param("precioMax") BigDecimal precioMax,
            @Param("tallas") List<String> tallas,
            @Param("colores") List<String> colores,
            @Param("genero") String genero,
            @Param("materiales") List<String> materiales,
            @Param("soloDisponibles") Boolean soloDisponibles,
            @Param("pesoMin") BigDecimal pesoMin,
            @Param("pesoMax") BigDecimal pesoMax,
            Pageable pageable);
    
    //metodo simplificado para obtener productos destacados con límite
    @Query(value = "SELECT * FROM productos WHERE activo = true ORDER BY fecha_creacion DESC LIMIT :limite", 
           nativeQuery = true)
    List<Producto> findProductosDestacados(@Param("limite") int limite);
}
