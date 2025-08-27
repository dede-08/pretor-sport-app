package com.pretor_sport.app.repository;

import com.pretor_sport.app.model.Cliente;
import com.pretor_sport.app.model.Venta;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface VentaRepository extends JpaRepository<Venta, Long> {
    //busca todas las ventas asociadas a un cliente en especifico
    List<Venta> findByPedido_Cliente(Cliente cliente);

    //busca todas las venetas realizadas dentro de un rango de fechas
    List<Venta> findByFechaVentaBetween(LocalDateTime fechaInicio, LocalDateTime fechaFin);
}