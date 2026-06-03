package com.scrumcore.controller;

import com.scrumcore.dto.CapacidadResponseDTO;
import com.scrumcore.dto.CoreResponseDTO;
import com.scrumcore.dto.SimuladorResponseDTO;
import com.scrumcore.service.CoreService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/core")
@CrossOrigin("*")
public class CoreController {

    private final CoreService coreService;

    public CoreController(CoreService coreService) {
        this.coreService = coreService;
    }

    // Motor original — analiza todos los sprints de proyectos activos
    @GetMapping("/analizar")
    public List<CoreResponseDTO> analizar() {
        return coreService.analizarSprints();
    }

    // Motor original — analiza un sprint específico
    @GetMapping("/analizar/{sprintId}")
    public ResponseEntity<?> analizarSprint(@PathVariable Long sprintId) {
        try {
            return ResponseEntity.ok(coreService.analizarSprintIndividual(sprintId));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // Capacity core — análisis de capacidad con días hábiles y freelancers
    @GetMapping("/capacidad/{sprintId}")
    public ResponseEntity<?> analizarCapacidad(@PathVariable Long sprintId) {
        try {
            return ResponseEntity.ok(coreService.analizarCapacidad(sprintId));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // Simulador — predice horas por tarea y riesgo del sprint
    @PostMapping("/simular")
    public ResponseEntity<?> simularSprint(@RequestBody Map<String, Object> body) {
        try {
            Long   sprintId         = ((Number) body.get("sprintId")).longValue();
            String nombreNuevaTarea = (String) body.get("nombreNuevaTarea");
            Integer horasNuevaTarea = body.get("horasNuevaTarea") != null
                    ? ((Number) body.get("horasNuevaTarea")).intValue() : null;

            return ResponseEntity.ok(
                coreService.simularSprint(sprintId, nombreNuevaTarea, horasNuevaTarea)
            );
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}