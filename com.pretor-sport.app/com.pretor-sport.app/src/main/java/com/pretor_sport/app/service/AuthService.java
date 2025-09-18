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
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final UsuarioRepository usuarioRepository;
    private final JwtUtil jwtUtil;
    private final PasswordEncoder passwordEncoder;
    private final UserDetailsService userDetailsService;

    @Value("${app.jwt.expiration:86400}")
    private Long jwtExpiration;

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
        if (usuarioRepository.existsByEmail(registroRequest.getEmail())) {
            throw new IllegalArgumentException("Ya existe un usuario con este email");
        }

        Usuario nuevoUsuario = new Usuario();
        nuevoUsuario.setNombre(registroRequest.getNombre());
        nuevoUsuario.setApellidos(registroRequest.getApellidos());
        nuevoUsuario.setEmail(registroRequest.getEmail());
        nuevoUsuario.setPassword(passwordEncoder.encode(registroRequest.getPassword()));
        nuevoUsuario.setDireccion(registroRequest.getDireccion());
        nuevoUsuario.setTelefono(registroRequest.getTelefono());
        nuevoUsuario.setRol(Usuario.Rol.ROLE_CLIENTE);
        nuevoUsuario.setActivo(true);
        nuevoUsuario.setEmailVerificado(false);
        nuevoUsuario.setTokenVerificacion(UUID.randomUUID().toString());

        Usuario usuarioGuardado = usuarioRepository.save(nuevoUsuario);

        usuarioGuardado.setEmailVerificado(true);
        usuarioGuardado.setTokenVerificacion(null);
        usuarioGuardado = usuarioRepository.save(usuarioGuardado);

        UserDetails userDetails = userDetailsService.loadUserByUsername(usuarioGuardado.getEmail());
        String accessToken = jwtUtil.generateToken(userDetails, usuarioGuardado.getId(), usuarioGuardado.getRol().name());
        String refreshToken = jwtUtil.generateRefreshToken(userDetails, usuarioGuardado.getId(), usuarioGuardado.getRol().name());

        log.info("Nuevo usuario registrado: {} con rol: {}", usuarioGuardado.getEmail(), usuarioGuardado.getRol());

        return AuthResponseDTO.of(
            accessToken,
            refreshToken,
            jwtExpiration,
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
            Long userId = jwtUtil.extractUserId(refreshToken);
            String rol = jwtUtil.extractRole(refreshToken);

            Usuario usuario = usuarioRepository.findByEmailAndActivo(username, true)
                .orElseThrow(() -> new BadCredentialsException("Usuario no encontrado o inactivo"));

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

    public void logout(String accessToken) {
        try {
            String username = jwtUtil.extractUsername(accessToken);
            log.info("Usuario desconectado: {}", username);
        } catch (Exception e) {
            log.warn("Error al procesar logout: {}", e.getMessage());
        }
    }
}