package com.scrumcore.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "usuarios")
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String nombre;

    @Column(unique = true, nullable = false, length = 150)
    private String correo;

    @Column(nullable = false)
    private String password;

    // Valores: ROLE_ADMIN, ROLE_SCRUM_MASTER, ROLE_DEVELOPER, ROLE_FREELANCER
    @Column(nullable = false, length = 30)
    private String rol;

    // Horas reales disponibles por sprint (clave para el motor)
    @Column(nullable = false)
    private Integer horasDisponibles;
}