package com.scrumcore.controller;

import com.scrumcore.entity.Sprint;
import com.scrumcore.service.SprintService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/sprints")
@CrossOrigin("*")
public class SprintController {

    private final SprintService sprintService;

    public SprintController(SprintService sprintService) {
        this.sprintService = sprintService;
    }

    @GetMapping
    public List<Sprint> listar() {
        return sprintService.listar();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Sprint> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(sprintService.buscarPorId(id));
    }

    @GetMapping("/proyecto/{proyectoId}")
    public List<Sprint> listarPorProyecto(@PathVariable Long proyectoId) {
        return sprintService.listarPorProyecto(proyectoId);
    }

    @PostMapping
    public ResponseEntity<Sprint> guardar(@RequestBody Sprint sprint) {
        return ResponseEntity.ok(sprintService.guardar(sprint));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Sprint> actualizar(@PathVariable Long id,
                                              @RequestBody Sprint sprint) {
        return ResponseEntity.ok(sprintService.actualizar(id, sprint));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        sprintService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}