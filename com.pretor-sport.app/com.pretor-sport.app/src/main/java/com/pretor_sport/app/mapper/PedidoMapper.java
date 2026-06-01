package com.pretor_sport.app.mapper;

import com.pretor_sport.app.dto.response.PedidoResponseDTO;
import com.pretor_sport.app.model.DetallePedido;
import com.pretor_sport.app.model.Pedido;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface PedidoMapper {

    @Mapping(target = "usuarioId", source = "usuario.id")
    @Mapping(target = "usuarioNombre", expression = "java(pedido.getUsuario().getNombreCompleto())")
    @Mapping(target = "total", expression = "java(pedido.getSubtotal())")
    PedidoResponseDTO toResponseDTO(Pedido pedido);

    @Mapping(target = "productoId", source = "producto.id")
    @Mapping(target = "productoNombre", source = "producto.nombre")
    @Mapping(target = "productoImagenUrl", source = "producto.imagenUrl")
    @Mapping(target = "subtotal", expression = "java(detalle.getSubtotal())")
    PedidoResponseDTO.DetallePedidoResponseDTO toDetalleResponseDTO(DetallePedido detalle);
}
