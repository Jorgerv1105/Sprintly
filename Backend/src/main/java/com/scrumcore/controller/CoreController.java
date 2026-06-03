package com.scrumcore.controller;

import com.scrumcore.dto.CoreResponseDTO;
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

    @GetMapping("/analizar")
    public List<CoreResponseDTO> analizar() {
        return coreService.analizarSprints();
    }

    @GetMapping("/analizar/{sprintId}")
    public ResponseEntity<?> analizarSprint(@PathVariable Long sprintId) {
        try {
            return ResponseEntity.ok(coreService.analizarSprintIndividual(sprintId));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}