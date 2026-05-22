package com.pretor_sport.app.mapper;

import com.pretor_sport.app.dto.response.ProductoResponseDTO;
import com.pretor_sport.app.model.Producto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ProductoMapper {

    @Mapping(target = "disponible", source = "disponible")
    @Mapping(target = "estadoStock", source = "estadoStock")
    @Mapping(target = "genero", expression = "java(producto.getGenero() != null ? producto.getGenero().name() : null)")
    ProductoResponseDTO toResponseDTO(Producto producto);
}
