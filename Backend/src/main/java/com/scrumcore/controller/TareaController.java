package com.scrumcore.controller;

import com.scrumcore.entity.Tarea;
import com.scrumcore.service.TareaService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

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
    
    @GetMapping("/usuario/{usuarioId}")
    public List<Tarea> listarPorUsuario(@PathVariable Long usuarioId) {
        return tareaService.listarPorUsuario(usuarioId);
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
    @PatchMapping("/{id}/estado")
    public ResponseEntity<Tarea> actualizarEstado(@PathVariable Long id,
    @RequestBody Map<String, String> body) {
        Tarea tarea = tareaService.buscarPorId(id);
        tarea.setEstado(body.get("estado"));
        return ResponseEntity.ok(tareaService.guardar(tarea));
    }
}