package com.scrumcore.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ContratacionDTO {
    private String nombreFreelancer;
    private String especialidad;
    private double tarifaHora;
    private int    horasAsignadas;
    private double costo;
}