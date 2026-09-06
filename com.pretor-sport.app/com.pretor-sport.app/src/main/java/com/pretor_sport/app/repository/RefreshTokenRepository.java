package com.pretor_sport.app.repository;

import com.pretor_sport.app.model.RefreshToken;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Optional;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

    Optional<RefreshToken> findByTokenHashAndRevocadoFalse(String tokenHash);

    boolean existsByUsuario_IdAndTokenHash(Long usuarioId, String tokenHash);

    @Modifying
    @Query("UPDATE RefreshToken rt SET rt.revocado = true " +
           "WHERE rt.usuario.id = :usuarioId AND rt.revocado = false")
    int revokeAllForUser(@Param("usuarioId") Long usuarioId);

    @Modifying
    @Query("UPDATE RefreshToken rt SET rt.revocado = true WHERE rt.id = :id AND rt.revocado = false")
    int revokeIfActive(@Param("id") Long id);

    @Transactional
    void deleteByExpiresAtBefore(LocalDateTime cutoff);
}