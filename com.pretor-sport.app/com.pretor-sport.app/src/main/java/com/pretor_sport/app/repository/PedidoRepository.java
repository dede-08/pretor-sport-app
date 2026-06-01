package com.pretor_sport.app.repository;

import com.pretor_sport.app.model.Usuario;
import com.pretor_sport.app.model.Pedido;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface PedidoRepository extends JpaRepository<Pedido, Long> {
    Optional<Pedido> findByUsuarioAndEstado(Usuario usuario, String estado);
    java.util.List<Pedido> findByUsuarioAndEstadoNot(Usuario usuario, String estado);
}