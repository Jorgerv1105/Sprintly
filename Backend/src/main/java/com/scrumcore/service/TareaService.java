package com.scrumcore.service;

import com.scrumcore.entity.Tarea;
import com.scrumcore.repository.TareaRepository;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class TareaService {

    private final TareaRepository tareaRepository;

    public TareaService(TareaRepository tareaRepository) {
        this.tareaRepository = tareaRepository;
    }

    public List<Tarea> listar() {
        return tareaRepository.findAll();
    }

    public Tarea guardar(Tarea tarea) {
        return tareaRepository.save(tarea);
    }

    public Tarea buscarPorId(Long id) {
        return tareaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Tarea no encontrada con id: " + id));
    }

    public Tarea actualizar(Long id, Tarea datosNuevos) {
        Tarea existente = buscarPorId(id);
        existente.setNombre(datosNuevos.getNombre());
        existente.setHorasNecesarias(datosNuevos.getHorasNecesarias());
        existente.setEstado(datosNuevos.getEstado());
        existente.setSprint(datosNuevos.getSprint());
        existente.setUsuarioAsignado(datosNuevos.getUsuarioAsignado());
        return tareaRepository.save(existente);
    }

    public void eliminar(Long id) {
        tareaRepository.deleteById(id);
    }

    public List<Tarea> listarPorSprint(Long sprintId) {
        return tareaRepository.findBySprintId(sprintId);
    }
}