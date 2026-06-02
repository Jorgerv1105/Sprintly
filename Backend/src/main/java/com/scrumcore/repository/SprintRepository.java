package com.scrumcore.repository;

import com.scrumcore.entity.Sprint;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface SprintRepository extends JpaRepository<Sprint, Long> {
    List<Sprint> findByProyectoId(Long proyectoId);
}