package com.scrumcore.service;

import com.scrumcore.entity.Proyecto;
import com.scrumcore.repository.ProyectoRepository;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class ProyectoService {

    private final ProyectoRepository proyectoRepository;

    public ProyectoService(ProyectoRepository proyectoRepository) {
        this.proyectoRepository = proyectoRepository;
    }

    public List<Proyecto> listar() {
        return proyectoRepository.findAll();
    }

    public Proyecto guardar(Proyecto proyecto) {
        return proyectoRepository.save(proyecto);
    }

    public Proyecto buscarPorId(Long id) {
        return proyectoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Proyecto no encontrado con id: " + id));
    }

    public Proyecto actualizar(Long id, Proyecto datosNuevos) {
        Proyecto existente = buscarPorId(id);
        existente.setNombre(datosNuevos.getNombre());
        existente.setEstado(datosNuevos.getEstado());
        existente.setDescripcion(datosNuevos.getDescripcion());
        return proyectoRepository.save(existente);
    }

    public void eliminar(Long id) {
        proyectoRepository.deleteById(id);
    }

    public List<Proyecto> listarPorEstado(String estado) {
        return proyectoRepository.findByEstado(estado);
    }
}