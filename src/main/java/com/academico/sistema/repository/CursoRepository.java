package com.academico.sistema.repository;

import com.academico.sistema.entity.Curso;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface CursoRepository extends JpaRepository<Curso, Long> {

    @Query("select distinct c from Curso c left join fetch c.estudiantes")
    List<Curso> findAllConEstudiantes();

    @Query("select c from Curso c left join fetch c.estudiantes where c.id = :id")
    Optional<Curso> findByIdConEstudiantes(@Param("id") Long id);
}
