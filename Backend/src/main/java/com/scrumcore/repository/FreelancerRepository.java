package com.scrumcore.repository;

import com.scrumcore.entity.Freelancer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface FreelancerRepository extends JpaRepository<Freelancer, Long> {

    // Para el motor: solo freelancers activos disponibles
    List<Freelancer> findByActivoTrue();

    // Para el panel: buscar por usuario
    Freelancer findByUsuarioId(Long usuarioId);
}