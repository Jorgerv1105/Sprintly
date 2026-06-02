package com.scrumcore.controller;

import com.scrumcore.dto.CoreResponseDTO;
import com.scrumcore.service.CoreService;
import org.springframework.web.bind.annotation.*;
import java.util.List;

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
}