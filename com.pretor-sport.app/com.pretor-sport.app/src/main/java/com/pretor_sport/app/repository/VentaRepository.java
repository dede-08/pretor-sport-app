package com.pretor_sport.app.repository;

import com.pretor_sport.app.model.Usuario;
import com.pretor_sport.app.model.Venta;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface VentaRepository extends JpaRepository<Venta, Long> {

    //busca todas las ventas asociadas a un cliente en especifico
    @EntityGraph(attributePaths = {"pedido.usuario", "pago"})
    Page<Venta> findByPedido_Usuario(Usuario usuario, Pageable pageable);

    //busca todas las ventas realizadas dentro de un rango de fechas
    List<Venta> findByFechaVentaBetween(LocalDateTime fechaInicio, LocalDateTime fechaFin);

    @Override
    @EntityGraph(attributePaths = {"pedido.usuario", "pago"})
    Page<Venta> findAll(Pageable pageable);
}
