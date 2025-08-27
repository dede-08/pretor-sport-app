package com.pretor_sport.app.repository;


import com.pretor_sport.app.model.Producto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductoRepository extends JpaRepository<Producto, Long> {
    //busca productos cuyo nombre contenga el termino de busqueda, ignorando mayusculas y minusculas en la busqueda
    Page<Producto> findByNombreContainingIgnoreCase(String nombre, Pageable pageable);
}
