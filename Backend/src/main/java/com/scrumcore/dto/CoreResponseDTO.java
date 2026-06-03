package com.scrumcore.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CoreResponseDTO {

    // ── Datos del Sprint ────────────────────────────────
    private String nombreSprint;
    private String nombreProyecto;

    // ── Resultado 1: Similitud ──────────────────────────
    private int          porcentajeSimilitud;
    private String       alertaRiesgo;
    private String       razonSimilitud;         // Explicación de por qué ese puntaje
    private List<DetalleHistoricoDTO> detalleHistorico; // Qué proyectos se compararon

    // ── Resultado 2: Carga laboral ──────────────────────
    private int horasNecesarias;
    private int horasDisponibles;
    private int deficit;
    private double riesgoOperativo;              // (deficit / horasNecesarias) * 100
    private String viabilidad;                   // VIABLE o DEFICITARIO
    private List<EquipoInternoDTO> equipoInterno; // Detalle por developer

    // ── Resultado 3: Freelancer ─────────────────────────
    private boolean freelancerAsignado;
    private String  nombreFreelancer;
    private String  especialidadFreelancer;
    private int     horasAsignadasFreelancer;
    private double  costoEstimadoFreelancer;
}