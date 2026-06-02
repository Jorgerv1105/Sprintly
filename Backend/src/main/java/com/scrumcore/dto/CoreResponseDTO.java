package com.scrumcore.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CoreResponseDTO {

    private String nombreSprint;
    private String nombreProyecto;

    private int    porcentajeSimilitud;
    private String alertaRiesgo;

    private int horasNecesarias;
    private int horasDisponibles;
    private int deficit;

    private boolean freelancerAsignado;
    private String  nombreFreelancer;
    private String  especialidadFreelancer;
    private int     horasAsignadasFreelancer;
    private double  costoEstimadoFreelancer;
}