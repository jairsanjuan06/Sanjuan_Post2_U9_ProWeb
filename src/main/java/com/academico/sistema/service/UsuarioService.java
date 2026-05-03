package com.academico.sistema.service;

import com.academico.sistema.model.Usuario;
import com.academico.sistema.repository.UsuarioRepository;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;


@Service
public class UsuarioService {

    private final UsuarioRepository repo;
    private final PasswordEncoder encoder;

    public UsuarioService(UsuarioRepository repo, PasswordEncoder encoder) {
        this.repo = repo;
        this.encoder = encoder;
    }

    @Transactional
    public void registrar(Usuario usuario) {
        if (repo.existsByEmail(usuario.getEmail())) {
            throw new RuntimeException("El correo ya está registrado");
        }
        usuario.setContrasenia(encoder.encode(usuario.getContrasenia()));
        usuario.setRol("ROLE_USER");
        usuario.setActivo(true);
        repo.save(usuario);
    }

    @PreAuthorize("hasRole('ADMIN')")
    public List<Usuario> listarTodos() {
        return repo.findAll();
    }


    @PreAuthorize("hasRole('ADMIN') or #email == authentication.name")
    public Optional<Usuario> buscarPorEmail(String email) {
        return repo.findByEmail(email);
    }


    @PreAuthorize("hasRole('ADMIN')")
    @Transactional
    public void cambiarRol(Long id, String nuevoRol) {
        Usuario u = repo.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        u.setRol(nuevoRol);
    }

  
    @PreAuthorize("#usuario.email == authentication.name or hasRole('ADMIN')")
    @Transactional
    public void actualizarNombre(Usuario usuario) {
        Usuario existente = repo.findById(usuario.getId())
                .orElseThrow(() -> new RuntimeException("No encontrado"));
        existente.setNombre(usuario.getNombre());
    }
}
