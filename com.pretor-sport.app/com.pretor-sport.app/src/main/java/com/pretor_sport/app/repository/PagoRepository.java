package com.pretor_sport.app.repository;

import com.pretor_sport.app.model.Pago;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PagoRepository extends JpaRepository<Pago, Long> {
    //se puede añadir busquedas por metodo de pago o estado si es necesario mas adelante
}