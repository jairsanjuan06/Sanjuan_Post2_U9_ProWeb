package com.academico.sistema.config;

import com.academico.sistema.service.UsuarioDetailsService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity   // Habilita @PreAuthorize en servicios y controladores
public class SecurityConfig {

    // ─── BCryptPasswordEncoder ────────────────────────────────────────────────
    // Factor 12 → 2^12 = 4096 iteraciones. Valor recomendado en 2026.
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(12);
    }

    // ─── DaoAuthenticationProvider ───────────────────────────────────────────
    // Conecta UserDetailsService con BCryptPasswordEncoder.
    // Spring Security llama a loadUserByUsername() y luego verifica la
    // contraseña con passwordEncoder.matches(raw, hash) — nunca texto claro.
    @Bean
    public DaoAuthenticationProvider authProvider(
            UsuarioDetailsService uds, PasswordEncoder pe) {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(uds);
        provider.setPasswordEncoder(pe);
        return provider;
    }

    // ─── SecurityFilterChain ─────────────────────────────────────────────────
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            // ── Autorización por URL ────────────────────────────────────────
            .authorizeHttpRequests(auth -> auth
                // Rutas públicas: sin autenticación requerida
                .requestMatchers("/", "/login", "/registro",
                                 "/css/**", "/js/**", "/images/**").permitAll()
                // Solo rol ADMIN puede acceder a /admin/**
                .requestMatchers("/admin/**").hasRole("ADMIN")
                // Cualquier otra ruta requiere estar autenticado
                .anyRequest().authenticated()
            )

            // ── Form Login ──────────────────────────────────────────────────
            .formLogin(form -> form
                .loginPage("/login")                        // Vista personalizada
                .loginProcessingUrl("/login")               // Spring procesa el POST
                .defaultSuccessUrl("/dashboard", true)      // Tras login exitoso
                .failureUrl("/login?error=true")            // Tras credenciales incorrectas
                .permitAll()
            )

            // ── Logout ──────────────────────────────────────────────────────
            .logout(logout -> logout
                .logoutUrl("/logout")
                .logoutSuccessUrl("/login?logout")
                .invalidateHttpSession(true)                // Destruye la sesión HTTP
                .deleteCookies("JSESSIONID")                // Elimina la cookie de sesión
                .permitAll()
            )

            // ── Gestión de sesión ───────────────────────────────────────────
            .sessionManagement(session -> session
                // Crea nueva sesión al autenticar → previene session fixation
                .sessionFixation(fix -> fix.migrateSession())
                // Máximo 1 sesión concurrente por usuario
                .maximumSessions(1)
                .maxSessionsPreventsLogin(false)            // Nueva sesión invalida la vieja
            );

        return http.build();
    }
}
