package com.scrumcore.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "tareas")
public class Tarea {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 150)
    private String nombre;

    // Horas necesarias para completar esta tarea (input del motor de carga)
    @Column(name = "horas_necesarias", nullable = false)
    private Integer horasNecesarias;

    // Valores: PENDIENTE, EN_PROGRESO, COMPLETADA
    @Column(nullable = false, length = 20)
    private String estado;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sprint_id", nullable = false)
    private Sprint sprint;

    // Usuario asignado (puede ser DEVELOPER o FREELANCER)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id")
    private Usuario usuarioAsignado;
}