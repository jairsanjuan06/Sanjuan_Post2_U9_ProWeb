package com.academico.sistema.service;

import com.academico.sistema.entity.Curso;
import com.academico.sistema.entity.Estudiante;
import com.academico.sistema.repository.CursoRepository;
import com.academico.sistema.repository.EstudianteRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class CursoService {

    private final CursoRepository cursoRepository;
    private final EstudianteRepository estudianteRepository;

    public CursoService(CursoRepository cursoRepository,
                        EstudianteRepository estudianteRepository) {
        this.cursoRepository = cursoRepository;
        this.estudianteRepository = estudianteRepository;
    }

    @Transactional
    public Curso guardar(Curso curso) {
        return cursoRepository.save(curso);
    }

    @Transactional(readOnly = true)
    public List<Curso> listarCursosConEstudiantes() {
        return cursoRepository.findAllConEstudiantes();
    }

    @Transactional(readOnly = true)
    public Optional<Curso> buscarPorIdConEstudiantes(Long id) {
        return cursoRepository.findByIdConEstudiantes(id);
    }

    
    @Transactional
    public void inscribirEstudiante(Long cursoId, Long estudianteId) {

        Curso curso = cursoRepository.findById(cursoId)
                .orElseThrow(() -> new IllegalArgumentException("Curso no encontrado: " + cursoId));

        Estudiante estudiante = estudianteRepository.findById(estudianteId)
                .orElseThrow(() -> new IllegalArgumentException("Estudiante no encontrado: " + estudianteId));

        curso.agregarEstudiante(estudiante);
    }

    // 🔥 DESINSCRIPCIÓN (CORREGIDO)
    @Transactional
    public void desinscribirEstudiante(Long cursoId, Long estudianteId) {

        Curso curso = cursoRepository.findById(cursoId)
                .orElseThrow(() -> new IllegalArgumentException("Curso no encontrado: " + cursoId));

        Estudiante estudiante = estudianteRepository.findById(estudianteId)
                .orElseThrow(() -> new IllegalArgumentException("Estudiante no encontrado: " + estudianteId));

        curso.quitarEstudiante(estudiante);
    }
}