package com.pretor_sport.app.service;

import com.pretor_sport.app.model.Cliente;
import com.pretor_sport.app.repository.ClienteRepository;
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
public class UserDetailsServiceImpl implements UserDetailsService {

    private final ClienteRepository clienteRepository;

    public UserDetailsServiceImpl(ClienteRepository clienteRepository) {
        this.clienteRepository = clienteRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        // Buscamos el cliente por su email
        Cliente cliente = clienteRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("No se encontró un usuario con el email: " + email));

        // Creamos la lista de roles/autoridades
        GrantedAuthority authority = new SimpleGrantedAuthority(cliente.getRol());

        // Creamos y retornamos el objeto UserDetails que Spring Security utiliza
        return new User(cliente.getEmail(), cliente.getPassword(), Collections.singletonList(authority));
    }
}