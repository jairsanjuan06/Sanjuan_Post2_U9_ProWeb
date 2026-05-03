package com.academico.sistema.controller;

import com.academico.sistema.model.Usuario;
import com.academico.sistema.service.UsuarioService;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
public class AuthController {

    private final UsuarioService service;

    public AuthController(UsuarioService service) {
        this.service = service;
    }

    // ── Página de inicio ──────────────────────────────────────────────────────
    @GetMapping("/")
    public String inicio() {
        return "redirect:/login";
    }

    // ── Login ─────────────────────────────────────────────────────────────────
    // Spring Security procesa el POST /login automáticamente.
    // Solo necesitamos el GET para mostrar la vista personalizada.
    @GetMapping("/login")
    public String mostrarLogin() {
        return "auth/login";
    }

    // ── Registro ─────────────────────────────────────────────────────────────
    @GetMapping("/registro")
    public String mostrarRegistro(Model model) {
        model.addAttribute("usuario", new Usuario());
        return "auth/registro";
    }

    @PostMapping("/registro")
    public String registrar(@Valid @ModelAttribute("usuario") Usuario usuario,
                            BindingResult result) {
        if (result.hasErrors()) {
            return "auth/registro";   // Volver al formulario con errores de validación
        }
        try {
            service.registrar(usuario);
            return "redirect:/login?registrado";
        } catch (RuntimeException e) {
            // Email ya registrado → mostrar error en el campo
            result.rejectValue("email", "error.email", e.getMessage());
            return "auth/registro";
        }
    }

    // ── Dashboard (usuarios autenticados) ────────────────────────────────────
    @GetMapping("/dashboard")
    public String dashboard(Model model, Authentication auth) {
        model.addAttribute("usuario", auth.getName());
        model.addAttribute("roles", auth.getAuthorities());
        return "dashboard";
    }

    // ── Panel ADMIN ───────────────────────────────────────────────────────────
    // La URL /admin/** ya está protegida en SecurityConfig con hasRole("ADMIN").
    // Si un USER intenta acceder → Spring Security devuelve 403 automáticamente.
    @GetMapping("/admin")
    public String adminPanel(Model model) {
        model.addAttribute("usuarios", service.listarTodos());
        return "admin/panel";
    }
}
