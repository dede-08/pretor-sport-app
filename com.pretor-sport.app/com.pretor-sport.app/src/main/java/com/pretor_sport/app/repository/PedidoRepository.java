package com.pretor_sport.app.repository;

import com.pretor_sport.app.model.Usuario;
import com.pretor_sport.app.model.Pedido;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PedidoRepository extends JpaRepository<Pedido, Long> {

    @EntityGraph(attributePaths = {"usuario"})
    Page<Pedido> findByUsuario(Usuario usuario, Pageable pageable);

    @Override
    @EntityGraph(attributePaths = {"usuario"})
    Page<Pedido> findAll(Pageable pageable);
}
