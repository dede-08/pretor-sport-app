package com.pretor_sport.app.mapper;

import com.pretor_sport.app.dto.response.VentaResponseDTO;
import com.pretor_sport.app.model.Venta;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface VentaMapper {

    @Mapping(target = "pedidoId", source = "pedido.id")
    @Mapping(target = "usuarioId", source = "pedido.usuario.id")
    @Mapping(target = "usuarioNombre", expression = "java(venta.getPedido().getUsuario().getNombreCompleto())")
    @Mapping(target = "metodoPago", source = "pago.metodoPago")
    @Mapping(target = "estadoPago", source = "pago.estado")
    @Mapping(target = "idTransaccion", source = "pago.idTransaccion")
    VentaResponseDTO toResponseDTO(Venta venta);
}
