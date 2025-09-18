package com.pretor_sport.app.service;

import com.pretor_sport.app.model.Usuario;
import com.pretor_sport.app.repository.UsuarioRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;

@Service
@Slf4j
public class UserDetailsServiceImpl implements UserDetailsService {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    public void setUsuarioRepository(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        log.debug("Cargando usuario por email: {}", email);

        Usuario usuario = usuarioRepository.findByEmailAndActivo(email, true)
                .orElseThrow(() -> {
                    log.warn("Usuario no encontrado o inactivo: {}", email);
                    return new UsernameNotFoundException("No se encontró un usuario activo con el email: " + email);
                });

        GrantedAuthority authority = new SimpleGrantedAuthority(usuario.getRol().name());

        UserDetails userDetails = User.builder()
                .username(usuario.getEmail())
                .password(usuario.getPassword())
                .authorities(Collections.singletonList(authority))
                .accountExpired(false)
                .accountLocked(!usuario.getActivo())
                .credentialsExpired(false)
                .disabled(!usuario.getEmailVerificado())
                .build();

        log.debug("Usuario cargado exitosamente: {} con rol: {}", email, usuario.getRol());
        return userDetails;
    }
}