package com.scrumcore.service;

import com.scrumcore.entity.Freelancer;
import com.scrumcore.entity.Usuario;
import com.scrumcore.repository.FreelancerRepository;
import com.scrumcore.repository.UsuarioRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
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

    public List<Freelancer> listarActivos() {
        return freelancerRepository.findByActivoTrue();
    }

    // @Transactional garantiza que si falla el freelancer, se revierte el usuario también
    @Transactional
    public Freelancer crear(String nombre, String correo, String password,
                            String especialidad, Integer horasDisponibles,
                            Double costoHora) {

        // Verificar que el correo no exista
        if (usuarioRepository.findByCorreo(correo) != null) {
            throw new RuntimeException("El correo ya está registrado");
        }

        // 1. Crear usuario para login
        Usuario usuario = new Usuario();
        usuario.setNombre(nombre);
        usuario.setCorreo(correo);
        usuario.setPassword(passwordEncoder.encode(password));
        usuario.setRol("ROLE_FREELANCER");
        usuario.setHorasDisponibles(horasDisponibles);
        usuarioRepository.save(usuario);

        // 2. Crear perfil freelancer con datos extra
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
}