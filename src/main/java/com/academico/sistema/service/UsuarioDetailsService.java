package com.academico.sistema.service;

import com.academico.sistema.model.Usuario;
import com.academico.sistema.repository.UsuarioRepository;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

/**
 * Implementa la interfaz de Spring Security que carga datos del usuario
 * durante el proceso de autenticación.
 *
 * Flujo:
 *   FormLogin POST /login
 *     → DaoAuthenticationProvider
 *       → loadUserByUsername(email)   ← este método
 *         → passwordEncoder.matches(rawInput, hashEnBD)
 *           → SecurityContextHolder almacena Authentication
 */
@Service
public class UsuarioDetailsService implements UserDetailsService {

    private final UsuarioRepository repo;

    public UsuarioDetailsService(UsuarioRepository repo) {
        this.repo = repo;
    }

    @Override
    public UserDetails loadUserByUsername(String email)
            throws UsernameNotFoundException {

        // Buscar usuario en BD por email (usado como nombre de usuario)
        Usuario u = repo.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException(
                        "Usuario no encontrado: " + email));

        // Construir el UserDetails que Spring Security necesita.
        // .roles("ADMIN") agrega automáticamente el prefijo ROLE_
        // Por eso quitamos el prefijo que ya tiene en la BD.
        return User.builder()
                .username(u.getEmail())
                .password(u.getContrasenia())               // Ya es hash BCrypt
                .roles(u.getRol().replace("ROLE_", ""))     // "ADMIN" o "USER"
                .disabled(!u.isActivo())                    // Cuentas desactivadas
                .build();
    }
}
