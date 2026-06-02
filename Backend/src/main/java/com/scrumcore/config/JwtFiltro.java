package com.scrumcore.config;

import com.scrumcore.service.JwtService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component
public class JwtFiltro extends OncePerRequestFilter {

    private final JwtService jwtService;

    public JwtFiltro(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        String authHeader = request.getHeader("Authorization");

        // Si no hay token simplemente dejamos pasar
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = authHeader.substring(7);

        if (jwtService.esValido(token)) {
            String correo = jwtService.extraerCorreo(token);
            String rol    = jwtService.extraerRol(token);

            // Registramos al usuario autenticado en el contexto de Spring Security
            UsernamePasswordAuthenticationToken auth =
                new UsernamePasswordAuthenticationToken(
                    correo,
                    null,
                    List.of(new SimpleGrantedAuthority(rol))
                );

            SecurityContextHolder.getContext().setAuthentication(auth);
        }

        filterChain.doFilter(request, response);
    }
}