package com.scrumcore.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CapacidadResponseDTO {

    private String nombreSprint;
    private String nombreProyecto;

    // Fechas y tiempo
    private long   diasHabiles;
    private double semanas;

    // Histórico
    private int    proyectosHistoricosAnalizados;
    private double horasEstimadas;
    private List<DetalleHistoricoCapacidadDTO> detalleHistorico;

    // Equipo interno
    private double horasInternas;
    private List<EquipoCapacidadDTO> equipoInterno;

    // Déficit
    private double  deficit;
    private boolean hayDeficit;
    private double  riesgoOperativo;
    private String  viabilidad;

    // Freelancers sugeridos
    private double costoExtraTotal;
    private List<ContratacionDTO> contratacionesSugeridas;
}