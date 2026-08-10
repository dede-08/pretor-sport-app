package com.pretor_sport.app.service;

import com.pretor_sport.app.dto.request.CategoriaRequestDTO;
import com.pretor_sport.app.dto.response.CategoriaResponseDTO;
import com.pretor_sport.app.mapper.CategoriaMapper;
import com.pretor_sport.app.model.Categoria;
import com.pretor_sport.app.repository.CategoriaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
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
public class CategoriaService {

    private final CategoriaRepository categoriaRepository;
    private final CategoriaMapper categoriaMapper;

    @Transactional(readOnly = true)
    @Cacheable(value = "categorias", key = "'all_active'")
    public List<CategoriaResponseDTO> listarCategoriasActivas() {
        log.debug("Listando todas las categorías activas");
        return categoriaRepository.findByActivaTrue().stream()
                .map(categoriaMapper::toResponseDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Page<CategoriaResponseDTO> listarTodas(Pageable pageable) {
        log.debug("Listando todas las categorías");
        Page<Categoria> page = categoriaRepository.findAll(pageable);
        List<CategoriaResponseDTO> dtos = page.getContent().stream()
                .map(categoriaMapper::toResponseDTO)
                .collect(Collectors.toList());
        return new PageImpl<>(dtos, page.getPageable(), page.getTotalElements());
    }

    @Transactional(readOnly = true)
    @Cacheable(value = "categorias", key = "#id")
    public CategoriaResponseDTO obtenerPorId(Long id) {
        log.debug("Obteniendo categoría con ID: {}", id);
        return categoriaRepository.findById(id)
                .map(categoriaMapper::toResponseDTO)
                .orElseThrow(() -> new IllegalArgumentException("Categoría no encontrada con ID: " + id));
    }

    @Transactional
    @CacheEvict(value = "categorias", allEntries = true)
    public CategoriaResponseDTO crear(CategoriaRequestDTO request) {
        log.info("Creando nueva categoría: {}", request.getNombre());
        
        if (categoriaRepository.existsByNombre(request.getNombre())) {
            throw new IllegalArgumentException("Ya existe una categoría con el nombre: " + request.getNombre());
        }

        Categoria categoria = categoriaMapper.toEntity(request);
        Categoria guardada = categoriaRepository.save(categoria);
        return categoriaMapper.toResponseDTO(guardada);
    }

    @Transactional
    @CacheEvict(value = "categorias", allEntries = true)
    public CategoriaResponseDTO actualizar(Long id, CategoriaRequestDTO request) {
        log.info("Actualizando categoría con ID: {}", id);
        
        Categoria categoria = categoriaRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Categoría no encontrada con ID: " + id));

        categoriaMapper.updateFromDTO(request, categoria);
        Categoria actualizada = categoriaRepository.save(categoria);
        return categoriaMapper.toResponseDTO(actualizada);
    }

    @Transactional
    @CacheEvict(value = "categorias", allEntries = true)
    public void eliminar(Long id) {
        log.info("Desactivando categoría con ID: {}", id);
        Categoria categoria = categoriaRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Categoría no encontrada con ID: " + id));
        
        categoria.setActiva(false);
        categoriaRepository.save(categoria);
    }
}
