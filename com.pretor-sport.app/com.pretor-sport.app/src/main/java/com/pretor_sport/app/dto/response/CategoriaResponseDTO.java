package com.pretor_sport.app.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CategoriaResponseDTO {
    private Long id;
    private String nombre;
    private String descripcion;
    private String iconoUrl;
    private String tipo;
    private Integer orden;
    private Boolean activa;
    private long productosCount;
    private LocalDateTime fechaCreacion;
}
