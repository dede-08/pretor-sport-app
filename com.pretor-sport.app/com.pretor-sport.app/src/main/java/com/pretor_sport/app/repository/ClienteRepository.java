package com.pretor_sport.app.repository;

import com.pretor_sport.app.model.Cliente;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ClienteRepository extends JpaRepository<Cliente, Long> {
    
    Optional<Cliente> findByEmail(String email);
    
    Optional<Cliente> findByEmailAndActivo(String email, Boolean activo);
    
    boolean existsByEmail(String email);
    
    Optional<Cliente> findByTokenVerificacion(String token);
    
    List<Cliente> findByRol(Cliente.Rol rol);
    
    Page<Cliente> findByActivoAndRol(Boolean activo, Cliente.Rol rol, Pageable pageable);
    
    @Query("SELECT c FROM Cliente c WHERE c.activo = :activo")
    Page<Cliente> findByActivo(@Param("activo") Boolean activo, Pageable pageable);
    
    @Query("SELECT c FROM Cliente c WHERE " +
           "(LOWER(c.nombre) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(c.apellidos) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(c.email) LIKE LOWER(CONCAT('%', :search, '%'))) AND " +
           "c.activo = :activo")
    Page<Cliente> findBySearchAndActivo(@Param("search") String search, 
                                       @Param("activo") Boolean activo, 
                                       Pageable pageable);
    
    @Modifying
    @Query("UPDATE Cliente c SET c.ultimoAcceso = :fechaAcceso WHERE c.email = :email")
    void updateUltimoAcceso(@Param("email") String email, 
                           @Param("fechaAcceso") LocalDateTime fechaAcceso);
    
    @Modifying
    @Query("UPDATE Cliente c SET c.emailVerificado = true, c.tokenVerificacion = null WHERE c.tokenVerificacion = :token")
    int verificarEmail(@Param("token") String token);
    
    @Query("SELECT COUNT(c) FROM Cliente c WHERE c.rol = :rol AND c.activo = true")
    long countByRolAndActivo(@Param("rol") Cliente.Rol rol);
    
    @Query("SELECT c FROM Cliente c WHERE c.fechaRegistro BETWEEN :inicio AND :fin")
    List<Cliente> findByFechaRegistroBetween(@Param("inicio") LocalDateTime inicio, 
                                           @Param("fin") LocalDateTime fin);
}
