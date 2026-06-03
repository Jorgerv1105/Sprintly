package com.scrumcore.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EquipoCapacidadDTO {
    private String nombre;
    private double horasDiarias;
    private double horasDisponibles;
}