package com.pretor_sport.app.service;

import com.pretor_sport.app.dto.request.ProveedorRequestDTO;
import com.pretor_sport.app.dto.response.ProveedorResponseDTO;
import com.pretor_sport.app.mapper.ProveedorMapper;
import com.pretor_sport.app.model.Proveedor;
import com.pretor_sport.app.repository.ProveedorRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProveedorService {

    private final ProveedorRepository proveedorRepository;
    private final ProveedorMapper proveedorMapper;

    @Transactional(readOnly = true)
    public Page<ProveedorResponseDTO> listarTodos(Pageable pageable) {
        log.debug("Listando todos los proveedores");
        Page<Proveedor> page = proveedorRepository.findAll(pageable);
        List<ProveedorResponseDTO> dtos = page.getContent().stream()
                .map(proveedorMapper::toResponseDTO)
                .collect(Collectors.toList());
        return new PageImpl<>(dtos, page.getPageable(), page.getTotalElements());
    }

    @Transactional(readOnly = true)
    public ProveedorResponseDTO obtenerPorId(Long id) {
        log.debug("Obteniendo proveedor con ID: {}", id);
        return proveedorRepository.findById(id)
                .map(proveedorMapper::toResponseDTO)
                .orElseThrow(() -> new IllegalArgumentException("Proveedor no encontrado con ID: " + id));
    }

    @Transactional
    public ProveedorResponseDTO crear(ProveedorRequestDTO request) {
        log.info("Creando nuevo proveedor: {}", request.getNombre());
        Proveedor proveedor = proveedorMapper.toEntity(request);
        Proveedor guardado = proveedorRepository.save(proveedor);
        return proveedorMapper.toResponseDTO(guardado);
    }

    @Transactional
    public ProveedorResponseDTO actualizar(Long id, ProveedorRequestDTO request) {
        log.info("Actualizando proveedor con ID: {}", id);
        Proveedor proveedor = proveedorRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Proveedor no encontrado con ID: " + id));

        proveedorMapper.updateFromDTO(request, proveedor);
        Proveedor actualizado = proveedorRepository.save(proveedor);
        return proveedorMapper.toResponseDTO(actualizado);
    }

    @Transactional
    public void eliminar(Long id) {
        log.info("Eliminando proveedor con ID: {}", id);
        if (!proveedorRepository.existsById(id)) {
            throw new IllegalArgumentException("Proveedor no encontrado con ID: " + id);
        }
        proveedorRepository.deleteById(id);
    }
}
