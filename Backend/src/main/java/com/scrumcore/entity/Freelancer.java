package com.scrumcore.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
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

    @Column(nullable = false, length = 100)
    private String especialidad;

    @Column(name = "horas_disponibles", nullable = false)
    private Integer horasDisponibles;

    @Column(name = "costo_hora", nullable = false)
    private Double costoHora;

    @Column(nullable = false)
    private Boolean activo;

    // JsonIgnoreProperties evita referencia circular al serializar
    @JsonIgnoreProperties({"tareas", "password"})
    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "usuario_id", nullable = false, unique = true)
    private Usuario usuario;
}