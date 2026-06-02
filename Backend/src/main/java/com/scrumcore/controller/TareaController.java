package com.scrumcore.controller;

import com.scrumcore.entity.Tarea;
import com.scrumcore.service.TareaService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/tareas")
@CrossOrigin("*")
public class TareaController {

    private final TareaService tareaService;

    public TareaController(TareaService tareaService) {
        this.tareaService = tareaService;
    }

    @GetMapping
    public List<Tarea> listar() {
        return tareaService.listar();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Tarea> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(tareaService.buscarPorId(id));
    }

    @GetMapping("/sprint/{sprintId}")
    public List<Tarea> listarPorSprint(@PathVariable Long sprintId) {
        return tareaService.listarPorSprint(sprintId);
    }

    @PostMapping
    public ResponseEntity<Tarea> guardar(@RequestBody Tarea tarea) {
        return ResponseEntity.ok(tareaService.guardar(tarea));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Tarea> actualizar(@PathVariable Long id,
                                             @RequestBody Tarea tarea) {
        return ResponseEntity.ok(tareaService.actualizar(id, tarea));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        tareaService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}