package com.pretor_sport.app.service;

import com.pretor_sport.app.model.RefreshToken;
import com.pretor_sport.app.model.Usuario;
import com.pretor_sport.app.repository.RefreshTokenRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.HexFormat;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;

    @Value("${app.jwt.refresh-expiration:604800}")
    private Long refreshExpiration;

    public String createRefreshToken(Usuario usuario) {
        String rawToken = UUID.randomUUID().toString() + "." + UUID.randomUUID().toString();

        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setUsuario(usuario);
        refreshToken.setTokenHash(hash(rawToken));
        refreshToken.setExpiresAt(LocalDateTime.now().plusSeconds(refreshExpiration));
        refreshToken.setRevocado(false);
        refreshToken.setFechaCreacion(LocalDateTime.now());
        refreshTokenRepository.save(refreshToken);

        return rawToken;
    }

    @Transactional
    public Optional<RefreshToken> findActiveByToken(String rawToken) {
        return refreshTokenRepository.findByTokenHashAndRevocadoFalse(hash(rawToken));
    }

    @Transactional
    public boolean revokeIfActive(Long id) {
        return refreshTokenRepository.revokeIfActive(id) > 0;
    }

    @Transactional
    public int revokeAllForUser(Long usuarioId) {
        return refreshTokenRepository.revokeAllForUser(usuarioId);
    }

    @Transactional
    public void cleanExpired() {
        refreshTokenRepository.deleteByExpiresAtBefore(LocalDateTime.now());
    }

    private String hash(String rawToken) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = digest.digest(rawToken.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hashBytes);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("Algoritmo de hash SHA-256 no disponible", e);
        }
    }
}