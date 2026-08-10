package com.pretor_sport.app.repository;

import com.pretor_sport.app.model.Usuario;
import com.pretor_sport.app.model.Pedido;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface PedidoRepository extends JpaRepository<Pedido, Long> {

    @EntityGraph(attributePaths = {"usuario"})
    Optional<Pedido> findByUsuarioAndEstado(Usuario usuario, String estado);

    @EntityGraph(attributePaths = {"usuario"})
    Page<Pedido> findByUsuarioAndEstadoNot(Usuario usuario, String estado, Pageable pageable);

    @Override
    @EntityGraph(attributePaths = {"usuario"})
    Page<Pedido> findAll(Pageable pageable);
}
