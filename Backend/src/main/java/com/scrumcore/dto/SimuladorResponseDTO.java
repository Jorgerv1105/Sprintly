package com.scrumcore.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SimuladorResponseDTO {
    private String sprint;
    private String proyecto;
    private double sprintCapacity;
    private double totalHorasPredichas;
    private String sprintRisk;        // Bajo, Medio, Alto
    private int    tareasAnalizadas;
    private List<TareaPredichaDTO> resultados;
}