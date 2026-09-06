package com.pretor_sport.app.repository;

import com.pretor_sport.app.model.Categoria;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CategoriaRepository extends JpaRepository<Categoria, Long> {
    boolean existsByNombre(String nombre);
    List<Categoria> findByActivaTrue();

    //cuenta productos disponibles (activos y con stock) por categoría en una sola consulta
    @Query("SELECT c.id, COUNT(p) FROM Categoria c LEFT JOIN c.productos p " +
           "ON p.activo = true AND p.stock > 0 GROUP BY c.id")
    List<Object[]> contarProductosActivosPorCategoria();

    @Query("SELECT COUNT(p) FROM Categoria c JOIN c.productos p " +
           "WHERE c.id = :id AND p.activo = true AND p.stock > 0")
    long contarProductosActivos(@Param("id") Long id);
}

