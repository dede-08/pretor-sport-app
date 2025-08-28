package com.pretor_sport.app.service;

import com.pretor_sport.app.model.Cliente;
import com.pretor_sport.app.repository.ClienteRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
@RequiredArgsConstructor
@Slf4j
public class UserDetailsServiceImpl implements UserDetailsService {

    private final ClienteRepository clienteRepository;

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        log.debug("Cargando usuario por email: {}", email);
        
        // Buscamos el cliente por su email y que esté activo
        Cliente cliente = clienteRepository.findByEmailAndActivo(email, true)
                .orElseThrow(() -> {
                    log.warn("Usuario no encontrado o inactivo: {}", email);
                    return new UsernameNotFoundException("No se encontró un usuario activo con el email: " + email);
                });

        // Creamos la lista de roles/autoridades usando el enum
        GrantedAuthority authority = new SimpleGrantedAuthority(cliente.getRol().name());

        // Creamos y retornamos el objeto UserDetails que Spring Security utiliza
        UserDetails userDetails = User.builder()
                .username(cliente.getEmail())
                .password(cliente.getPassword())
                .authorities(Collections.singletonList(authority))
                .accountExpired(false)
                .accountLocked(!cliente.getActivo())
                .credentialsExpired(false)
                .disabled(!cliente.getEmailVerificado())
                .build();
        
        log.debug("Usuario cargado exitosamente: {} con rol: {}", email, cliente.getRol());
        return userDetails;
    }
}
