package com.pretor_sport.app.service;

import com.pretor_sport.app.dto.request.UsuarioRequestDTO;
import com.pretor_sport.app.dto.request.LoginRequestDTO;
import com.pretor_sport.app.dto.response.AuthResponseDTO;
import com.pretor_sport.app.model.RefreshToken;
import com.pretor_sport.app.model.Usuario;
import com.pretor_sport.app.repository.UsuarioRepository;
import com.pretor_sport.app.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final UsuarioService usuarioService;
    private final AuthenticationManager authenticationManager;
    private final UsuarioRepository usuarioRepository;
    private final JwtUtil jwtUtil;
    private final UserDetailsService userDetailsService;
    private final EmailService emailService;
    private final RefreshTokenService refreshTokenService;

    @Value("${app.jwt.expiration:86400}")
    private Long jwtExpiration;

    @Value("${app.frontend.url:http://localhost:4200}")
    private String frontendUrl;

    @Transactional
    public AuthResponseDTO login(LoginRequestDTO loginRequest) {
        try {
            authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                    loginRequest.getEmail(),
                    loginRequest.getPassword()
                )
            );
        } catch (BadCredentialsException e) {
            log.warn("Intento de login fallido para email: {}", loginRequest.getEmail());
            throw new BadCredentialsException("Credenciales inválidas");
        } catch (DisabledException e) {
            log.warn("Intento de login con cuenta deshabilitada: {}", loginRequest.getEmail());
            throw e;
        }

        Usuario usuario = usuarioRepository.findByEmailAndActivo(loginRequest.getEmail(), true)
            .orElseThrow(() -> new BadCredentialsException("Usuario no encontrado o inactivo"));

        if (!usuario.getEmailVerificado()) {
            throw new DisabledException("Email no verificado. Por favor, verifica tu email antes de continuar.");
        }

        usuarioRepository.updateUltimoAcceso(usuario.getEmail(), LocalDateTime.now());

        UserDetails userDetails = userDetailsService.loadUserByUsername(usuario.getEmail());
        String accessToken = jwtUtil.generateToken(userDetails, usuario.getId(), usuario.getRol().name());
        String refreshToken = refreshTokenService.createRefreshToken(usuario);

        log.info("Usuario autenticado exitosamente: {} con rol: {}", usuario.getEmail(), usuario.getRol());

        return AuthResponseDTO.of(
            accessToken,
            refreshToken,
            jwtExpiration,
            usuario.getId(),
            usuario.getNombre(),
            usuario.getApellidos(),
            usuario.getEmail(),
            usuario.getRol().name(),
            usuario.getEmailVerificado(),
            LocalDateTime.now()
        );
    }

    @Transactional
    public AuthResponseDTO register(UsuarioRequestDTO registroRequest) {
        Usuario usuarioGuardado = usuarioService.crearUsuario(registroRequest);

        try {
            emailService.sendVerificationEmail(
                usuarioGuardado.getEmail(), 
                usuarioGuardado.getNombre(), 
                usuarioGuardado.getTokenVerificacion()
            );
            log.info("Correo de verificación enviado a: {}", usuarioGuardado.getEmail());
        } catch (Exception e) {
            log.error("Error al enviar correo de verificación a {}: {}", usuarioGuardado.getEmail(), e.getMessage());
        }

        log.info("Nuevo usuario registrado (pendiente de verificación): {} con rol: {}", 
            usuarioGuardado.getEmail(), usuarioGuardado.getRol());

        return AuthResponseDTO.of(
            null,
            null,
            0L,
            usuarioGuardado.getId(),
            usuarioGuardado.getNombre(),
            usuarioGuardado.getApellidos(),
            usuarioGuardado.getEmail(),
            usuarioGuardado.getRol().name(),
            usuarioGuardado.getEmailVerificado(),
            LocalDateTime.now()
        );
    }

    @Transactional
    public AuthResponseDTO refreshToken(String refreshToken) {
        RefreshToken stored = refreshTokenService.findActiveByToken(refreshToken)
            .orElseThrow(() -> new BadCredentialsException("Sesión expirada o inválida. Inicia sesión de nuevo."));

        if (stored.getExpiresAt().isBefore(LocalDateTime.now())) {
            refreshTokenService.revokeIfActive(stored.getId());
            throw new BadCredentialsException("Sesión expirada. Inicia sesión de nuevo.");
        }

        Usuario usuario = stored.getUsuario();
        if (!usuario.getActivo()) {
            refreshTokenService.revokeIfActive(stored.getId());
            throw new BadCredentialsException("Cuenta deshabilitada. Inicia sesión de nuevo.");
        }
        if (!usuario.getEmailVerificado()) {
            refreshTokenService.revokeIfActive(stored.getId());
            throw new BadCredentialsException("Cuenta no verificada. Verifica tu email para renovar sesión.");
        }

        if (!refreshTokenService.revokeIfActive(stored.getId())) {
            throw new BadCredentialsException("Reutilización del token de refresh detectada. Inicia sesión de nuevo.");
        }

        UserDetails userDetails = userDetailsService.loadUserByUsername(usuario.getEmail());
        String newAccessToken = jwtUtil.generateToken(userDetails, usuario.getId(), usuario.getRol().name());
        String newRefreshToken = refreshTokenService.createRefreshToken(usuario);

        log.debug("Token renovado (con rotación) para usuario: {}", usuario.getEmail());

        return AuthResponseDTO.of(
            newAccessToken,
            newRefreshToken,
            jwtExpiration,
            usuario.getId(),
            usuario.getNombre(),
            usuario.getApellidos(),
            usuario.getEmail(),
            usuario.getRol().name(),
            usuario.getEmailVerificado(),
            usuario.getUltimoAcceso()
        );
    }

    @Transactional
    public boolean verificarEmail(String token) {
        int updated = usuarioRepository.verificarEmail(token);
        if (updated > 0) {
            log.info("Email verificado exitosamente para token: {}", token);
            return true;
        }
        log.warn("Token de verificación inválido: {}", token);
        return false;
    }

    public Usuario getCurrentUser(String email) {
        return usuarioRepository.findByEmailAndActivo(email, true)
            .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado o inactivo"));
    }

    @Transactional
    public String resendVerificationEmail(String email) {
        Usuario usuario = usuarioRepository.findByEmailAndActivo(email, true)
            .orElseThrow(() -> new IllegalArgumentException("No existe una cuenta activa con ese email"));

        if (Boolean.TRUE.equals(usuario.getEmailVerificado())) {
            throw new IllegalArgumentException("La cuenta ya está verificada");
        }

        String newToken = UUID.randomUUID().toString();
        usuario.setTokenVerificacion(newToken);
        usuarioRepository.save(usuario);

        try {
            emailService.sendVerificationEmail(usuario.getEmail(), usuario.getNombre(), newToken);
        } catch (Exception e) {
            log.error("Error al reenviar correo de verificación: {}", e.getMessage());
            throw new RuntimeException("No se pudo enviar el correo. Por favor, inténtalo de nuevo más tarde.");
        }

        String verificationUrl = frontendUrl + "/verify-email?token=" + newToken;
        log.info("Nuevo enlace de verificación generado para {}", email);

        return verificationUrl;
    }

    @Transactional
    public void logout(String email) {
        usuarioRepository.findByEmailAndActivo(email, true).ifPresent(usuario -> {
            int revocados = refreshTokenService.revokeAllForUser(usuario.getId());
            log.info("Usuario desconectado: {} (refresh tokens revocados: {})", email, revocados);
        });
    }
}
