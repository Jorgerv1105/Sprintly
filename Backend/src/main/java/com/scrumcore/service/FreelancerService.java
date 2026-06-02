package com.scrumcore.service;

import com.scrumcore.entity.Freelancer;
import com.scrumcore.entity.Usuario;
import com.scrumcore.repository.FreelancerRepository;
import com.scrumcore.repository.UsuarioRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class FreelancerService {

    private final FreelancerRepository freelancerRepository;
    private final UsuarioRepository    usuarioRepository;
    private final PasswordEncoder      passwordEncoder;

    public FreelancerService(FreelancerRepository freelancerRepository,
                             UsuarioRepository usuarioRepository,
                             PasswordEncoder passwordEncoder) {
        this.freelancerRepository = freelancerRepository;
        this.usuarioRepository    = usuarioRepository;
        this.passwordEncoder      = passwordEncoder;
    }

    public List<Freelancer> listar() {
        return freelancerRepository.findAll();
    }

    public Freelancer buscarPorId(Long id) {
        return freelancerRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Freelancer no encontrado: " + id));
    }

    public Freelancer buscarPorUsuarioId(Long usuarioId) {
        return freelancerRepository.findByUsuarioId(usuarioId);
    }

    // Crea el Usuario + Freelancer en una sola operación
    public Freelancer crear(String nombre, String correo, String password,
                            String especialidad, Integer horasDisponibles,
                            Double costoHora) {

        // 1. Crear el usuario para login
        Usuario usuario = new Usuario();
        usuario.setNombre(nombre);
        usuario.setCorreo(correo);
        usuario.setPassword(passwordEncoder.encode(password));
        usuario.setRol("ROLE_FREELANCER");
        usuario.setHorasDisponibles(horasDisponibles);
        usuarioRepository.save(usuario);

        // 2. Crear el perfil freelancer con datos extra
        Freelancer freelancer = new Freelancer();
        freelancer.setEspecialidad(especialidad);
        freelancer.setHorasDisponibles(horasDisponibles);
        freelancer.setCostoHora(costoHora);
        freelancer.setActivo(true);
        freelancer.setUsuario(usuario);
        return freelancerRepository.save(freelancer);
    }

    public Freelancer actualizar(Long id, Freelancer datosNuevos) {
        Freelancer existente = buscarPorId(id);
        existente.setEspecialidad(datosNuevos.getEspecialidad());
        existente.setHorasDisponibles(datosNuevos.getHorasDisponibles());
        existente.setCostoHora(datosNuevos.getCostoHora());
        existente.setActivo(datosNuevos.getActivo());
        return freelancerRepository.save(existente);
    }

    public void eliminar(Long id) {
        freelancerRepository.deleteById(id);
    }

    public List<Freelancer> listarActivos() {
        return freelancerRepository.findByActivoTrue();
    }
}