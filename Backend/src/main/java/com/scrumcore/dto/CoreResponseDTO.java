package com.scrumcore.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CoreResponseDTO {

    private String nombreSprint;
    private String nombreProyecto;

    private int horasNecesarias;
    private int horasDisponibles;

    private int deficit;

    private String viabilidad;

    private List<EquipoInternoDTO> equipoInterno;
}