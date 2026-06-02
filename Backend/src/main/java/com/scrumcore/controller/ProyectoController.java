package com.scrumcore.controller;

import com.scrumcore.entity.Proyecto;
import com.scrumcore.service.ProyectoService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/proyectos")
@CrossOrigin("*")
public class ProyectoController {

    private final ProyectoService proyectoService;

    public ProyectoController(ProyectoService proyectoService) {
        this.proyectoService = proyectoService;
    }

    @GetMapping
    public List<Proyecto> listar() {
        return proyectoService.listar();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Proyecto> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(proyectoService.buscarPorId(id));
    }

    @GetMapping("/estado/{estado}")
    public List<Proyecto> listarPorEstado(@PathVariable String estado) {
        return proyectoService.listarPorEstado(estado);
    }

    @PostMapping
    public ResponseEntity<Proyecto> guardar(@RequestBody Proyecto proyecto) {
        return ResponseEntity.ok(proyectoService.guardar(proyecto));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Proyecto> actualizar(@PathVariable Long id,
                                                @RequestBody Proyecto proyecto) {
        return ResponseEntity.ok(proyectoService.actualizar(id, proyecto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        proyectoService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}