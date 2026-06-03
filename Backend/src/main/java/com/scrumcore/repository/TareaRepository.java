package com.scrumcore.repository;

import com.scrumcore.entity.Tarea;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface TareaRepository extends JpaRepository<Tarea, Long> {
    List<Tarea> findBySprintId(Long sprintId);
    List<Tarea> findByUsuarioAsignadoId(Long usuarioId);
}