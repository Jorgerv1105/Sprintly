package com.scrumcore.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TareaPredichaDTO {
    private String nombre;
    private double horasEstimadas;
    private double horasPredichas;
    private double desviacion;
    private String confianza;          // alta, media, baja
    private boolean esNueva;
    private List<String> tareasSimilares;
}