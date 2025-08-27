package com.pretor_sport.app.service;

import com.pretor_sport.app.model.Cliente;
import com.pretor_sport.app.repository.ClienteRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import jakarta.persistence.EntityNotFoundException;
import java.util.Optional;

@Service
public class ClienteService {

    private final ClienteRepository clienteRepository;
    private final PasswordEncoder passwordEncoder;

    public ClienteService(ClienteRepository clienteRepository, PasswordEncoder passwordEncoder) {
        this.clienteRepository = clienteRepository;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * Registra un nuevo cliente en el sistema.
     * Verifica que el email no esté ya en uso y cifra la contraseña.
     */
    public Cliente registrarCliente(Cliente cliente) {
        if (clienteRepository.findByEmail(cliente.getEmail()).isPresent()) {
            throw new IllegalStateException("El email ya está registrado.");
        }
        // Ciframos la contraseña antes de guardarla
        cliente.setPassword(passwordEncoder.encode(cliente.getPassword()));
        // Asignamos el rol por defecto
        cliente.setRol("ROLE_CLIENTE");
        return clienteRepository.save(cliente);
    }

    //actualiza el perfil de un cliente existente
    public Cliente actualizarPerfil(Long id, Cliente datosActualizados) {
        Cliente clienteExistente = clienteRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Cliente no encontrado con id: " + id));

        clienteExistente.setNombre(datosActualizados.getNombre());
        clienteExistente.setApellidos(datosActualizados.getApellidos());
        clienteExistente.setDireccion(datosActualizados.getDireccion());
        clienteExistente.setTelefono(datosActualizados.getTelefono());

        return clienteRepository.save(clienteExistente);
    }

    public Optional<Cliente> buscarPorEmail(String email) {
        return clienteRepository.findByEmail(email);
    }
}