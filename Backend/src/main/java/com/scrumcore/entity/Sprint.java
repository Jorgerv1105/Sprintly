package com.scrumcore.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "sprints")
public class Sprint {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String nombre;

    // Necesarios para calcular duración y comparar con históricos
    @Column(name = "fecha_inicio", nullable = false)
    private LocalDate fechaInicio;

    @Column(name = "fecha_fin", nullable = false)
    private LocalDate fechaFin;

    // Horas estimadas totales del sprint (para la comparación de similitud)
    @Column(name = "horas_estimadas", nullable = false)
    private Integer horasEstimadas;

    // Valores: PLANIFICADO, EN_CURSO, COMPLETADO
    @Column(nullable = false, length = 20)
    private String estado;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "proyecto_id", nullable = false)
    private Proyecto proyecto;
}