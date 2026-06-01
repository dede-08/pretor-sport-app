package com.pretor_sport.app.mapper;

import com.pretor_sport.app.dto.request.ProveedorRequestDTO;
import com.pretor_sport.app.dto.response.ProveedorResponseDTO;
import com.pretor_sport.app.model.Proveedor;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface ProveedorMapper {

    ProveedorResponseDTO toResponseDTO(Proveedor proveedor);

    @Mapping(target = "id", ignore = true)
    Proveedor toEntity(ProveedorRequestDTO request);

    @Mapping(target = "id", ignore = true)
    void updateFromDTO(ProveedorRequestDTO request, @MappingTarget Proveedor proveedor);
}
