package com.pretor_sport.app.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Component
@Slf4j
public class JwtUtil {

    @Value("${app.jwt.secret:pretorSportSecretKeyForJWTTokenGenerationThatShouldBeLongEnough}")
    private String secret;

    @Value("${app.jwt.expiration:86400}") // 24 horas por defecto
    private Long jwtExpiration;

    @Value("${app.jwt.refresh-expiration:604800}") // 7 días por defecto
    private Long refreshExpiration;

    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(secret.getBytes());
    }

    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    public String extractRole(String token) {
        return extractClaim(token, claims -> claims.get("rol", String.class));
    }

    public Long extractUserId(String token) {
        return extractClaim(token, claims -> claims.get("userId", Long.class));
    }

    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    private Claims extractAllClaims(String token) {
        try {
            return Jwts.parser()
                    .verifyWith(getSigningKey())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (JwtException e) {
            log.error("Error al parsear JWT: {}", e.getMessage());
            throw e;
        }
    }

    private Boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    public String generateToken(UserDetails userDetails, Long userId, String rol) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", userId);
        claims.put("rol", rol);
        claims.put("type", "ACCESS");
        return createToken(claims, userDetails.getUsername(), jwtExpiration);
    }

    public String generateRefreshToken(UserDetails userDetails, Long userId, String rol) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", userId);
        claims.put("rol", rol);
        claims.put("type", "REFRESH");
        return createToken(claims, userDetails.getUsername(), refreshExpiration);
    }

    private String createToken(Map<String, Object> claims, String subject, Long expiration) {
        Instant now = Instant.now();
        Instant expiryDate = now.plus(expiration, ChronoUnit.SECONDS);

        return Jwts.builder()
                .claims(claims)
                .subject(subject)
                .issuedAt(Date.from(now))
                .expiration(Date.from(expiryDate))
                .signWith(getSigningKey(), Jwts.SIG.HS512)
                .compact();
    }

    public Boolean validateToken(String token, UserDetails userDetails) {
        try {
            final String username = extractUsername(token);
            return (username.equals(userDetails.getUsername()) && !isTokenExpired(token));
        } catch (JwtException e) {
            log.error("Token JWT inválido: {}", e.getMessage());
            return false;
        }
    }

    public Boolean isRefreshToken(String token) {
        try {
            String type = extractClaim(token, claims -> claims.get("type", String.class));
            return "REFRESH".equals(type);
        } catch (JwtException e) {
            log.error("Error al verificar tipo de token: {}", e.getMessage());
            return false;
        }
    }

    public Boolean validateTokenStructure(String token) {
        try {
            extractAllClaims(token);
            return !isTokenExpired(token);
        } catch (JwtException e) {
            log.error("Token JWT malformado o expirado: {}", e.getMessage());
            return false;
        }
    }

    public String refreshAccessToken(String refreshToken) {
        if (!isRefreshToken(refreshToken) || !validateTokenStructure(refreshToken)) {
            throw new IllegalArgumentException("Token de refresh inválido");
        }

        String username = extractUsername(refreshToken);
        Long userId = extractUserId(refreshToken);
        String rol = extractRole(refreshToken);

        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", userId);
        claims.put("rol", rol);
        claims.put("type", "ACCESS");

        return createToken(claims, username, jwtExpiration);
    }

    //metodo para extraer el token del header Authorization
    public String extractTokenFromHeader(String authHeader) {
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7);
        }
        return null;
    }

    //metodo para obtener informacion sensible sobre el token
    public Map<String, Object> getTokenInfo(String token) {
        try {
            Claims claims = extractAllClaims(token);
            Map<String, Object> info = new HashMap<>();
            info.put("username", claims.getSubject());
            info.put("userId", claims.get("userId"));
            info.put("rol", claims.get("rol"));
            info.put("type", claims.get("type"));
            info.put("issuedAt", claims.getIssuedAt());
            info.put("expiration", claims.getExpiration());
            info.put("expired", isTokenExpired(token));
            return info;
        } catch (JwtException e) {
            log.error("Error al obtener información del token: {}", e.getMessage());
            return null;
        }
    }
}
