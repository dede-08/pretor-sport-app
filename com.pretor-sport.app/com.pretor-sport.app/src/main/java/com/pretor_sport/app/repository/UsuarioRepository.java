package com.pretor_sport.app.repository;

import com.pretor_sport.app.model.Usuario;
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
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {
    
    Optional<Usuario> findByEmail(String email);
    
    Optional<Usuario> findByEmailAndActivo(String email, Boolean activo);
    
    boolean existsByEmail(String email);
    
    Optional<Usuario> findByTokenVerificacion(String token);
    
    List<Usuario> findByRol(Usuario.Rol rol);
    
    Page<Usuario> findByActivoAndRol(Boolean activo, Usuario.Rol rol, Pageable pageable);
    
    @Query("SELECT u FROM Usuario u WHERE u.activo = :activo")
    Page<Usuario> findByActivo(@Param("activo") Boolean activo, Pageable pageable);
    
    @Query("SELECT u FROM Usuario u WHERE " +
           "(LOWER(u.nombre) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(u.apellidos) LIKE LOWER(CONCAT('%', :search, '%')) OR " +
           "LOWER(u.email) LIKE LOWER(CONCAT('%', :search, '%'))) AND " +
           "u.activo = :activo")
    Page<Usuario> findBySearchAndActivo(@Param("search") String search, 
                                       @Param("activo") Boolean activo, 
                                       Pageable pageable);
    
    @Modifying
    @Query("UPDATE Usuario u SET u.ultimoAcceso = :fechaAcceso WHERE u.email = :email")
    void updateUltimoAcceso(@Param("email") String email, 
                           @Param("fechaAcceso") LocalDateTime fechaAcceso);
    
    @Modifying
    @Query("UPDATE Usuario u SET u.emailVerificado = true, u.tokenVerificacion = null WHERE u.tokenVerificacion = :token")
    int verificarEmail(@Param("token") String token);
    
    @Query("SELECT COUNT(u) FROM Usuario u WHERE u.rol = :rol AND u.activo = true")
    long countByRolAndActivo(@Param("rol") Usuario.Rol rol);
    
    @Query("SELECT u FROM Usuario u WHERE u.fechaRegistro BETWEEN :inicio AND :fin")
    List<Usuario> findByFechaRegistroBetween(@Param("inicio") LocalDateTime inicio, 
                                           @Param("fin") LocalDateTime fin);
}
