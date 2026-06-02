package com.scrumcore.repository;

import com.scrumcore.entity.Proyecto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ProyectoRepository extends JpaRepository<Proyecto, Long> {
    List<Proyecto> findByEstado(String estado);
}