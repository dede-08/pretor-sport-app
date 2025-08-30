package com.pretor_sport.app.service;

import com.pretor_sport.app.dto.request.ClienteRequestDTO;
import com.pretor_sport.app.dto.request.LoginRequestDTO;
import com.pretor_sport.app.dto.response.AuthResponseDTO;
import com.pretor_sport.app.model.Cliente;
import com.pretor_sport.app.repository.ClienteRepository;
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
    private final ClienteRepository clienteRepository;
    private final JwtUtil jwtUtil;
    private final PasswordEncoder passwordEncoder;
    private final UserDetailsService userDetailsService;

    @Value("${app.jwt.expiration:86400}")
    private Long jwtExpiration;

    //autenticar usuario y generar tokens JWT
    @Transactional
    public AuthResponseDTO login(LoginRequestDTO loginRequest) {
        try {
            // Intentar autenticar con las credenciales proporcionadas
            authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                    loginRequest.getEmail(),
                    loginRequest.getPassword()
                )
            );

            // Obtener el cliente de la base de datos
            Cliente cliente = clienteRepository.findByEmailAndActivo(loginRequest.getEmail(), true)
                .orElseThrow(() -> new BadCredentialsException("Usuario no encontrado o inactivo"));

            // Verificar si el email está verificado
            if (!cliente.getEmailVerificado()) {
                throw new DisabledException("Email no verificado. Por favor, verifica tu email antes de continuar.");
            }

            // Actualizar último acceso
            clienteRepository.updateUltimoAcceso(cliente.getEmail(), LocalDateTime.now());

            // Generar tokens
            UserDetails userDetails = userDetailsService.loadUserByUsername(cliente.getEmail());
            String accessToken = jwtUtil.generateToken(userDetails, cliente.getId(), cliente.getRol().name());
            String refreshToken = jwtUtil.generateRefreshToken(userDetails, cliente.getId(), cliente.getRol().name());

            log.info("Usuario autenticado exitosamente: {} con rol: {}", cliente.getEmail(), cliente.getRol());

            return AuthResponseDTO.of(
                accessToken,
                refreshToken,
                jwtExpiration,
                cliente.getId(),
                cliente.getNombre(),
                cliente.getApellidos(),
                cliente.getEmail(),
                cliente.getRol().name(),
                cliente.getEmailVerificado(),
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

    //registrar nuevo cliente
    @Transactional
    public AuthResponseDTO register(ClienteRequestDTO registroRequest) {
        // Verificar si el email ya existe
        if (clienteRepository.existsByEmail(registroRequest.getEmail())) {
            throw new IllegalArgumentException("Ya existe un usuario con este email");
        }

        // Crear nuevo cliente
        Cliente nuevoCliente = new Cliente();
        nuevoCliente.setNombre(registroRequest.getNombre());
        nuevoCliente.setApellidos(registroRequest.getApellidos());
        nuevoCliente.setEmail(registroRequest.getEmail());
        nuevoCliente.setPassword(passwordEncoder.encode(registroRequest.getPassword()));
        nuevoCliente.setDireccion(registroRequest.getDireccion());
        nuevoCliente.setTelefono(registroRequest.getTelefono());
        nuevoCliente.setRol(Cliente.Rol.ROLE_CLIENTE);
        //nuevoCliente.setRol(Cliente.Rol.valueOf(registroRequest.getRol() != null ?
        //                  registroRequest.getRol() : "ROLE_CLIENTE"));
        nuevoCliente.setActivo(true);
        nuevoCliente.setEmailVerificado(false); // En producción debería ser false
        nuevoCliente.setTokenVerificacion(UUID.randomUUID().toString());

        // Guardar el cliente
        Cliente clienteGuardado = clienteRepository.save(nuevoCliente);

        // TODO: Enviar email de verificación en producción
        // emailService.enviarEmailVerificacion(clienteGuardado.getEmail(), clienteGuardado.getTokenVerificacion());

        // Para desarrollo, verificar automáticamente
        clienteGuardado.setEmailVerificado(true);
        clienteGuardado.setTokenVerificacion(null);
        clienteGuardado = clienteRepository.save(clienteGuardado);

        // Generar tokens para auto-login después del registro
        UserDetails userDetails = userDetailsService.loadUserByUsername(clienteGuardado.getEmail());
        String accessToken = jwtUtil.generateToken(userDetails, clienteGuardado.getId(), clienteGuardado.getRol().name());
        String refreshToken = jwtUtil.generateRefreshToken(userDetails, clienteGuardado.getId(), clienteGuardado.getRol().name());

        log.info("Nuevo usuario registrado: {} con rol: {}", clienteGuardado.getEmail(), clienteGuardado.getRol());

        return AuthResponseDTO.of(
            accessToken,
            refreshToken,
            jwtExpiration,
            clienteGuardado.getId(),
            clienteGuardado.getNombre(),
            clienteGuardado.getApellidos(),
            clienteGuardado.getEmail(),
            clienteGuardado.getRol().name(),
            clienteGuardado.getEmailVerificado(),
            LocalDateTime.now()
        );
    }

    //renovar el access token usando refresh token
    public AuthResponseDTO refreshToken(String refreshToken) {
        try {
            if (!jwtUtil.isRefreshToken(refreshToken) || !jwtUtil.validateTokenStructure(refreshToken)) {
                throw new IllegalArgumentException("Refresh token inválido");
            }

            // Extraer información del refresh token
            String username = jwtUtil.extractUsername(refreshToken);
            Long userId = jwtUtil.extractUserId(refreshToken);
            String rol = jwtUtil.extractRole(refreshToken);

            // Verificar que el usuario aún existe y está activo
            Cliente cliente = clienteRepository.findByEmailAndActivo(username, true)
                .orElseThrow(() -> new BadCredentialsException("Usuario no encontrado o inactivo"));

            // Generar nuevo access token
            String newAccessToken = jwtUtil.refreshAccessToken(refreshToken);

            log.debug("Token renovado para usuario: {}", username);

            return AuthResponseDTO.of(
                newAccessToken,
                refreshToken, // El refresh token se mantiene igual
                jwtExpiration,
                cliente.getId(),
                cliente.getNombre(),
                cliente.getApellidos(),
                cliente.getEmail(),
                cliente.getRol().name(),
                cliente.getEmailVerificado(),
                cliente.getUltimoAcceso()
            );

        } catch (Exception e) {
            log.error("Error al renovar token: {}", e.getMessage());
            throw new IllegalArgumentException("Error al renovar token: " + e.getMessage());
        }
    }

    //verificar email con token
    @Transactional
    public boolean verificarEmail(String token) {
        int updated = clienteRepository.verificarEmail(token);
        if (updated > 0) {
            log.info("Email verificado exitosamente para token: {}", token);
            return true;
        }
        log.warn("Token de verificación inválido: {}", token);
        return false;
    }

    //obtener la informacion del usuario actual autenticado
    public Cliente getCurrentUser(String email) {
        return clienteRepository.findByEmailAndActivo(email, true)
            .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));
    }

    //logout(invalidad tokens)(mas adelante se implementará una blacklist)
    public void logout(String accessToken) {
        // En una implementación completa, agregarías el token a una blacklist
        // Por ahora solo logueamos la acción
        try {
            String username = jwtUtil.extractUsername(accessToken);
            log.info("Usuario desconectado: {}", username);
        } catch (Exception e) {
            log.warn("Error al procesar logout: {}", e.getMessage());
        }
    }
}
