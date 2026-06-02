package com.scrumcore.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "freelancers")
public class Freelancer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Especialidad del freelancer (ej: Backend, Frontend, QA)
    @Column(nullable = false, length = 100)
    private String especialidad;

    // Horas disponibles para el sprint
    @Column(name = "horas_disponibles", nullable = false)
    private Integer horasDisponibles;

    // Tarifa real por hora — clave para calcular costo en el motor
    @Column(name = "costo_hora", nullable = false)
    private Double costoHora;

    // Si está disponible para ser asignado
    @Column(nullable = false)
    private Boolean activo;

    // Relación con Usuario — un freelancer tiene un usuario para login
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", nullable = false, unique = true)
    private Usuario usuario;
}