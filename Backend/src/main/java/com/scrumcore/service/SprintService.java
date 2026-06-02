package com.scrumcore.service;

import com.scrumcore.entity.Sprint;
import com.scrumcore.repository.SprintRepository;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class SprintService {

    private final SprintRepository sprintRepository;

    public SprintService(SprintRepository sprintRepository) {
        this.sprintRepository = sprintRepository;
    }

    public List<Sprint> listar() {
        return sprintRepository.findAll();
    }

    public Sprint guardar(Sprint sprint) {
        return sprintRepository.save(sprint);
    }

    public Sprint buscarPorId(Long id) {
        return sprintRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Sprint no encontrado con id: " + id));
    }

    public Sprint actualizar(Long id, Sprint datosNuevos) {
        Sprint existente = buscarPorId(id);
        existente.setNombre(datosNuevos.getNombre());
        existente.setFechaInicio(datosNuevos.getFechaInicio());
        existente.setFechaFin(datosNuevos.getFechaFin());
        existente.setHorasEstimadas(datosNuevos.getHorasEstimadas());
        existente.setEstado(datosNuevos.getEstado());
        existente.setProyecto(datosNuevos.getProyecto());
        return sprintRepository.save(existente);
    }

    public void eliminar(Long id) {
        sprintRepository.deleteById(id);
    }

    public List<Sprint> listarPorProyecto(Long proyectoId) {
        return sprintRepository.findByProyectoId(proyectoId);
    }
}