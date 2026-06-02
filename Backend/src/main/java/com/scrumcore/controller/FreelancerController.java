package com.scrumcore.controller;

import com.scrumcore.entity.Freelancer;
import com.scrumcore.service.FreelancerService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/freelancers")
@CrossOrigin("*")
public class FreelancerController {

    private final FreelancerService freelancerService;

    public FreelancerController(FreelancerService freelancerService) {
        this.freelancerService = freelancerService;
    }

    @GetMapping
    public List<Freelancer> listar() {
        return freelancerService.listar();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Freelancer> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(freelancerService.buscarPorId(id));
    }

    @GetMapping("/activos")
    public List<Freelancer> listarActivos() {
        return freelancerService.listarActivos();
    }

    @GetMapping("/usuario/{usuarioId}")
    public ResponseEntity<Freelancer> buscarPorUsuario(@PathVariable Long usuarioId) {
        return ResponseEntity.ok(freelancerService.buscarPorUsuarioId(usuarioId));
    }

    @PostMapping
    public ResponseEntity<?> crear(@RequestBody Map<String, Object> body) {
        try {
            Freelancer f = freelancerService.crear(
                (String)  body.get("nombre"),
                (String)  body.get("correo"),
                (String)  body.get("password"),
                (String)  body.get("especialidad"),
                (Integer) body.get("horasDisponibles"),
                ((Number) body.get("costoHora")).doubleValue()
            );
            return ResponseEntity.ok(f);
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<Freelancer> actualizar(@PathVariable Long id,
                                                  @RequestBody Freelancer freelancer) {
        return ResponseEntity.ok(freelancerService.actualizar(id, freelancer));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        freelancerService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}