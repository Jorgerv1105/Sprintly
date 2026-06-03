package com.scrumcore.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EquipoInternoDTO {
    private String nombre;
    private String rol;
    private int    horasDisponibles;
}