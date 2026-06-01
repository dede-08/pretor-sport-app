package com.pretor_sport.app.mapper;

import com.pretor_sport.app.dto.response.UsuarioResponseDTO;
import com.pretor_sport.app.model.Usuario;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface UsuarioMapper {
    UsuarioResponseDTO toResponseDTO(Usuario usuario);
}
