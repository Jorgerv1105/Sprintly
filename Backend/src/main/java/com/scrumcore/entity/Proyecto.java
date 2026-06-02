package com.scrumcore.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "proyectos")
public class Proyecto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String nombre;

    // Clave para el motor: separa históricos de actuales
    // Valores: ACTIVO, HISTORICO
    @Column(nullable = false, length = 20)
    private String estado;

    @Column(length = 300)
    private String descripcion;
}