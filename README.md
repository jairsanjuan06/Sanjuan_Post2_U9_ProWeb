# Sanjuan-post2-u9 

**Unidad 9: Seguridad en Aplicaciones Web — Post-Contenido 2**  
Programación Web · Ingeniería de Sistemas · UDES 2026

> Extensión del Post-Contenido 1. Requiere el proyecto anterior funcionando
> con login, registro y roles ADMIN/USER operativos.

---

## Objetivo

Verificar activamente las protecciones de seguridad implementadas en el
Post-Contenido 1, añadiendo:

1. Autorización a nivel de método con `@PreAuthorize` y distintas expresiones SpEL
2. Página de error 403 personalizada
3. Mitigación de XSS con `th:text` en Thymeleaf
4. Cabecera Content Security Policy (CSP)
5. Verificación activa de la protección CSRF

---

## Archivos modificados o creados en este taller

| Archivo | Tipo | Descripción |
|---------|------|-------------|
| `service/UsuarioService.java` | Modificado | 4 métodos con `@PreAuthorize` |
| `config/SecurityConfig.java` | Modificado | `exceptionHandling` + CSP header |
| `controller/ErrorController.java` | Nuevo | Maneja `/error/403` |
| `controller/AuthController.java` | Modificado | Pasa `nombreUsuario` al dashboard |
| `templates/error/403.html` | Nuevo | Vista de acceso denegado |
| `templates/dashboard.html` | Modificado | Demo XSS con `th:text` |

---

## Configuración y ejecución

Los prerrequisitos son los mismos del Post-Contenido 1:

```bash
# Base de datos ya configurada del taller anterior
# Solo ejecutar:
mvn spring-boot:run
```


## Métodos protegidos con @PreAuthorize

| Método | Expresión SpEL | Significado |
|--------|---------------|-------------|
| `listarTodos()` | `hasRole('ADMIN')` | Solo ADMIN puede listar usuarios |
| `buscarPorEmail(email)` | `hasRole('ADMIN') or #email == authentication.name` | ADMIN o el propio usuario |
| `cambiarRol(id, rol)` | `hasRole('ADMIN')` | Solo ADMIN cambia roles |
| `actualizarNombre(usuario)` | `#usuario.email == authentication.name or hasRole('ADMIN')` | Dueño del perfil o ADMIN |

> `@EnableMethodSecurity` debe estar presente en `SecurityConfig` para que
> `@PreAuthorize` funcione. Sin esta anotación, las restricciones son ignoradas.


**Verificación en Chrome DevTools:**
`F12 → Network → seleccionar cualquier request → Response Headers`

Cabecera enviada por el servidor:
```
Content-Security-Policy: default-src 'self'; script-src 'self';
style-src 'self' 'unsafe-inline'; img-src 'self' data:; frame-ancestors 'none'
```

**Qué protege cada directiva:**

| Directiva | Valor | Protección |
|-----------|-------|------------|
| `default-src` | `'self'` | Solo recursos del mismo origen por defecto |
| `script-src` | `'self'` | Bloquea scripts inline y de dominios externos |
| `style-src` | `'self' 'unsafe-inline'` | Permite estilos inline (necesario para las vistas) |
| `img-src` | `'self' data:` | Imágenes propias y data URIs |
| `frame-ancestors` | `'none'` | Protege contra clickjacking (equivale a X-Frame-Options: DENY) |

**Resultado:** el servidor envía la cabecera CSP en todas las respuestas.
Un script inyectado desde un dominio externo sería bloqueado por el navegador
incluso si lograse insertarse en el HTML.

---

**Por qué funciona:**
Spring Security compara el token en la petición con el almacenado en la
`HttpSession` del servidor. Si no coinciden o no existe, rechaza la petición
con 403. Un sitio malicioso que intente hacer un POST en nombre del usuario
autenticado no puede incluir el token porque no tiene acceso a la sesión.

**Resultado:** `Status: 403`. El servidor rechaza el POST sin token.
Los formularios normales de la aplicación funcionan correctamente porque
Thymeleaf incluye el token automáticamente.

---

## Resumen de protecciones activas

| Protección | Mecanismo | Verificado |
|------------|-----------|------------|
| Autorización por método | `@PreAuthorize` + `@EnableMethodSecurity` | ✓ |
| Error 403 personalizado | `.accessDeniedPage("/error/403")` | ✓ |
| XSS en salida | `th:text` en todas las vistas | ✓ |
| Content Security Policy | `.contentSecurityPolicy(...)` en headers | ✓ |
| CSRF en formularios | Token automático vía `th:action` | ✓ |
| Session Fixation | `.sessionFixation(fix -> fix.migrateSession())` | ✓ (Post-Contenido 1) |
| Contraseñas hasheadas | `BCryptPasswordEncoder(12)` | ✓ (Post-Contenido 1) |
