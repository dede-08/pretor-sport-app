package com.pretor_sport.app.repository;

import com.pretor_sport.app.model.Cliente;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ClienteRepository extends JpaRepository<Cliente, Long> {
    //buscar un cliente por su email
    Optional<Cliente> findByEmail(String email);
}
