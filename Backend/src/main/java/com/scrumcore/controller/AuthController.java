package com.scrumcore.controller;

import com.scrumcore.entity.Usuario;
import com.scrumcore.repository.UsuarioRepository;
import com.scrumcore.service.JwtService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/auth")
@CrossOrigin("*")
public class AuthController {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder   passwordEncoder;
    private final JwtService        jwtService;

    public AuthController(UsuarioRepository usuarioRepository,
                          PasswordEncoder passwordEncoder,
                          JwtService jwtService) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder   = passwordEncoder;
        this.jwtService        = jwtService;
    }

    // ── LOGIN ────────────────────────────────────────────
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> body) {
        try {
            String correo   = body.get("correo");
            String password = body.get("password");

            Usuario usuario = usuarioRepository.findByCorreo(correo);

            if (usuario == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("error", "Usuario no encontrado"));
            }

            if (!passwordEncoder.matches(password, usuario.getPassword())) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("error", "Contraseña incorrecta"));
            }

            // Generamos JWT real con correo y rol
            String token = jwtService.generateToken(
                usuario.getCorreo(), usuario.getRol()
            );

            return ResponseEntity.ok(Map.of(
                "token",  token,
                "rol",    usuario.getRol(),
                "nombre", usuario.getNombre()
            ));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    // ── REGISTRO (solo DEVELOPER por defecto) ───────────
    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody Map<String, String> body) {
        try {
            String correo = body.get("correo");

            // Verificar que el correo no exista
            if (usuarioRepository.findByCorreo(correo) != null) {
                return ResponseEntity.status(HttpStatus.CONFLICT)
                        .body(Map.of("error", "El correo ya está registrado"));
            }

            Usuario nuevo = new Usuario();
            nuevo.setNombre(body.get("nombre"));
            nuevo.setCorreo(correo);
            nuevo.setPassword(passwordEncoder.encode(body.get("password")));
            nuevo.setRol("ROLE_DEVELOPER");
            nuevo.setHorasDisponibles(40);

            usuarioRepository.save(nuevo);

            return ResponseEntity.ok(Map.of(
                "mensaje", "Usuario registrado correctamente"
            ));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        }
    }
}