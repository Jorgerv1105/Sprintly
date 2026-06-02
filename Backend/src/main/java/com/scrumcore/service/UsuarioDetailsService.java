package com.scrumcore.service;

import com.scrumcore.entity.Usuario;
import com.scrumcore.repository.UsuarioRepository;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class UsuarioDetailsService implements UserDetailsService {

    private final UsuarioRepository usuarioRepository;

    public UsuarioDetailsService(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String correo)
            throws UsernameNotFoundException {

        Usuario usuario = usuarioRepository.findByCorreo(correo);

        if (usuario == null) {
            throw new UsernameNotFoundException(
                "Usuario no encontrado: " + correo
            );
        }

        // Spring Security necesita el rol sin el prefijo ROLE_ internamente
        return User.builder()
                .username(usuario.getCorreo())
                .password(usuario.getPassword())
                .roles(usuario.getRol().replace("ROLE_", ""))
                .build();
    }
}