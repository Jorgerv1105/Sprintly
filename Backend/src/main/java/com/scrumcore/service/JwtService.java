package com.scrumcore.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;

@Service
public class JwtService {

    private static final String SECRET =
        "scrumcore_clave_secreta_muy_larga_2024_segura";

    private static final long EXPIRACION_MS = 86400000; // 24 horas

    private Key getKey() {
        return Keys.hmacShaKeyFor(SECRET.getBytes(StandardCharsets.UTF_8));
    }

    // Genera el token con correo y rol dentro
    public String generateToken(String correo, String rol) {
        return Jwts.builder()
                .setSubject(correo)
                .claim("rol", rol)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRACION_MS))
                .signWith(getKey())
                .compact();
    }

    // Extrae el correo del token
    public String extraerCorreo(String token) {
        return getClaims(token).getSubject();
    }

    // Extrae el rol del token
    public String extraerRol(String token) {
        return getClaims(token).get("rol", String.class);
    }

    // Valida si el token es válido y no expiró
    public boolean esValido(String token) {
        try {
            return getClaims(token).getExpiration().after(new Date());
        } catch (Exception e) {
            return false;
        }
    }

    private Claims getClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
}