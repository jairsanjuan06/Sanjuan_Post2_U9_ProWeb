package com.academico.sistema.service;

import com.academico.sistema.model.Usuario;
import com.academico.sistema.repository.UsuarioRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class UsuarioService {

    private final UsuarioRepository repo;
    private final PasswordEncoder encoder;

    // Inyección por constructor — buena práctica con Spring
    public UsuarioService(UsuarioRepository repo, PasswordEncoder encoder) {
        this.repo = repo;
        this.encoder = encoder;
    }

    /**
     * Registra un nuevo usuario aplicando hashing BCrypt a la contraseña.
     * NUNCA se guarda la contraseña en texto claro en la base de datos.
     */
    @Transactional
    public void registrar(Usuario usuario) {
        if (repo.existsByEmail(usuario.getEmail())) {
            throw new RuntimeException("El correo ya está registrado");
        }
        // Hashear contraseña antes de persistir
        // encoder.encode() aplica BCrypt con salt aleatorio → resultado único por llamada
        usuario.setContrasenia(encoder.encode(usuario.getContrasenia()));
        usuario.setRol("ROLE_USER");   // Rol por defecto al registrarse
        usuario.setActivo(true);
        repo.save(usuario);
    }

    /**
     * Lista todos los usuarios — usado por el panel de ADMIN.
     */
    public List<Usuario> listarTodos() {
        return repo.findAll();
    }
}
