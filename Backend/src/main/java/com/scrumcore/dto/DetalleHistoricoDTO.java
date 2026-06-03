package com.scrumcore.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DetalleHistoricoDTO {
    private String      proyectoNombre;
    private String      sprintNombre;
    private int         puntajeObtenido;
    private List<String> reglasAplicadas;
}