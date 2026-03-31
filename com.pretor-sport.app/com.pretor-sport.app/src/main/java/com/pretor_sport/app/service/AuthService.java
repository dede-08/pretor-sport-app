package com.pretor_sport.app.service;

import com.pretor_sport.app.dto.request.UsuarioRequestDTO;
import com.pretor_sport.app.dto.request.LoginRequestDTO;
import com.pretor_sport.app.dto.response.AuthResponseDTO;
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

            Usuario usuario = usuarioRepository.findByEmailAndActivo(loginRequest.getEmail(), true)
                .orElseThrow(() -> new BadCredentialsException("Usuario no encontrado o inactivo"));

            if (!usuario.getEmailVerificado()) {
                throw new DisabledException("Email no verificado. Por favor, verifica tu email antes de continuar.");
            }

            usuarioRepository.updateUltimoAcceso(usuario.getEmail(), LocalDateTime.now());

            UserDetails userDetails = userDetailsService.loadUserByUsername(usuario.getEmail());
            String accessToken = jwtUtil.generateToken(userDetails, usuario.getId(), usuario.getRol().name());
            String refreshToken = jwtUtil.generateRefreshToken(userDetails, usuario.getId(), usuario.getRol().name());

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

        } catch (BadCredentialsException e) {
            log.warn("Intento de login fallido para email: {}", loginRequest.getEmail());
            throw new BadCredentialsException("Credenciales inválidas");
        } catch (DisabledException e) {
            log.warn("Intento de login con cuenta deshabilitada: {}", loginRequest.getEmail());
            throw e;
        } catch (Exception e) {
            log.error("Error durante la autenticación para email: {}", loginRequest.getEmail(), e);
            throw new RuntimeException("Error interno durante la autenticación");
        }
    }

    @Transactional
    public AuthResponseDTO register(UsuarioRequestDTO registroRequest) {
        Usuario usuarioGuardado = usuarioService.crearUsuario(registroRequest);

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

    public AuthResponseDTO refreshToken(String refreshToken) {
        try {
            if (!jwtUtil.isRefreshToken(refreshToken) || !jwtUtil.validateTokenStructure(refreshToken)) {
                throw new IllegalArgumentException("Refresh token inválido");
            }

            String username = jwtUtil.extractUsername(refreshToken);

            Usuario usuario = usuarioRepository.findByEmailAndActivo(username, true)
                .orElseThrow(() -> new BadCredentialsException("Usuario no encontrado o inactivo"));

            if (!usuario.getEmailVerificado()) {
                throw new DisabledException("Cuenta no verificada. Verifica tu email para renovar sesión.");
            }

            String newAccessToken = jwtUtil.refreshAccessToken(refreshToken);

            log.debug("Token renovado para usuario: {}", username);

            return AuthResponseDTO.of(
                newAccessToken,
                refreshToken,
                jwtExpiration,
                usuario.getId(),
                usuario.getNombre(),
                usuario.getApellidos(),
                usuario.getEmail(),
                usuario.getRol().name(),
                usuario.getEmailVerificado(),
                usuario.getUltimoAcceso()
            );

        } catch (Exception e) {
            log.error("Error al renovar token: {}", e.getMessage());
            throw new IllegalArgumentException("Error al renovar token: " + e.getMessage());
        }
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
            .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));
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

        String verificationUrl = frontendUrl + "/verify-email?token=" + newToken;
        log.info("Nuevo enlace de verificación generado para {}: {}", email, verificationUrl);

        return verificationUrl;
    }

    public void logout(String accessToken) {
        try {
            String username = jwtUtil.extractUsername(accessToken);
            log.info("Usuario desconectado: {}", username);
        } catch (Exception e) {
            log.warn("Error al procesar logout: {}", e.getMessage());
        }
    }
}