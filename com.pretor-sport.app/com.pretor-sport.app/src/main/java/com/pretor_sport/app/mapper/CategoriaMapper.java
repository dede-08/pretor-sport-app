package com.pretor_sport.app.mapper;

import com.pretor_sport.app.dto.request.CategoriaRequestDTO;
import com.pretor_sport.app.dto.response.CategoriaResponseDTO;
import com.pretor_sport.app.model.Categoria;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface CategoriaMapper {

    @Mapping(target = "productosCount", expression = "java(categoria.getProductosActivosCount())")
    CategoriaResponseDTO toResponseDTO(Categoria categoria);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "fechaCreacion", ignore = true)
    @Mapping(target = "fechaActualizacion", ignore = true)
    @Mapping(target = "productos", ignore = true)
    Categoria toEntity(CategoriaRequestDTO request);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "fechaCreacion", ignore = true)
    @Mapping(target = "fechaActualizacion", ignore = true)
    @Mapping(target = "productos", ignore = true)
    void updateFromDTO(CategoriaRequestDTO request, @MappingTarget Categoria categoria);
}
