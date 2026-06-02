package com.scrumcore.service;

import com.scrumcore.entity.Usuario;
import com.scrumcore.repository.UsuarioRepository;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;

    public UsuarioService(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    public List<Usuario> listar() {
        return usuarioRepository.findAll();
    }

    public Usuario guardar(Usuario usuario) {
        return usuarioRepository.save(usuario);
    }

    public Usuario buscarPorId(Long id) {
        return usuarioRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado con id: " + id));
    }

    public Usuario actualizar(Long id, Usuario datosNuevos) {
        Usuario existente = buscarPorId(id);
        existente.setNombre(datosNuevos.getNombre());
        existente.setCorreo(datosNuevos.getCorreo());
        existente.setRol(datosNuevos.getRol());
        existente.setHorasDisponibles(datosNuevos.getHorasDisponibles());
        return usuarioRepository.save(existente);
    }

    public void eliminar(Long id) {
        usuarioRepository.deleteById(id);
    }

    public List<Usuario> listarPorRol(String rol) {
        return usuarioRepository.findByRol(rol);
    }
}