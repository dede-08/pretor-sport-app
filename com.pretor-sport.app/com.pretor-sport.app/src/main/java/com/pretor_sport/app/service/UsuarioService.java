package com.pretor_sport.app.service;

import com.pretor_sport.app.dto.request.UsuarioRequestDTO;
import com.pretor_sport.app.model.Usuario;
import com.pretor_sport.app.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import jakarta.persistence.EntityNotFoundException;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public Usuario crearUsuario(UsuarioRequestDTO dto) {
        if (usuarioRepository.existsByEmail(dto.getEmail())) {
            throw new IllegalArgumentException("El email ya está registrado.");
        }

        Usuario usuario = new Usuario();
        usuario.setNombre(dto.getNombre());
        usuario.setApellidos(dto.getApellidos());
        usuario.setEmail(dto.getEmail());
        usuario.setPassword(passwordEncoder.encode(dto.getPassword()));
        usuario.setDireccion(dto.getDireccion());
        usuario.setTelefono(dto.getTelefono());
        usuario.setRol(Usuario.Rol.ROLE_CLIENTE);
        usuario.setActivo(true);
        usuario.setEmailVerificado(false); // Por defecto falso para requerir verificación
        usuario.setTokenVerificacion(UUID.randomUUID().toString());

        return usuarioRepository.save(usuario);
    }

    @Transactional
    public Usuario registrarUsuario(Usuario usuario) {
        if (usuarioRepository.existsByEmail(usuario.getEmail())) {
            throw new IllegalStateException("El email ya está registrado.");
        }
        usuario.setPassword(passwordEncoder.encode(usuario.getPassword()));
        usuario.setRol(Usuario.Rol.ROLE_CLIENTE);
        return usuarioRepository.save(usuario);
    }

    @Transactional
    public Usuario actualizarPerfil(Long id, Usuario datosActualizados) {
        Usuario usuarioExistente = usuarioRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Usuario no encontrado con id: " + id));

        usuarioExistente.setNombre(datosActualizados.getNombre());
        usuarioExistente.setApellidos(datosActualizados.getApellidos());
        usuarioExistente.setDireccion(datosActualizados.getDireccion());
        usuarioExistente.setTelefono(datosActualizados.getTelefono());

        return usuarioRepository.save(usuarioExistente);
    }

    public Optional<Usuario> buscarPorEmail(String email) {
        return usuarioRepository.findByEmail(email);
    }
}
