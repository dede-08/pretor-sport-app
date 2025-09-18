package com.pretor_sport.app.repository;

import com.pretor_sport.app.model.Usuario;
import com.pretor_sport.app.model.Pedido;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface PedidoRepository extends JpaRepository<Pedido, Long> {
    //busca un pedido de un cliente especifico que tenga un estado determinado
    Optional<Pedido> findByUsuarioAndEstado(Usuario usuario, String estado);
}