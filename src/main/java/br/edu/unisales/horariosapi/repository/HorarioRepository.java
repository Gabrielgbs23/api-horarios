package br.edu.unisales.horariosapi.repository;

import br.edu.unisales.horariosapi.model.DiaSemana;
import br.edu.unisales.horariosapi.model.Horario;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalTime;
import java.util.List;

public interface HorarioRepository extends JpaRepository<Horario, Long> {

    @Override
    @EntityGraph(attributePaths = {"professor", "disciplina", "turma"})
    List<Horario> findAll();

    @EntityGraph(attributePaths = {"professor", "disciplina", "turma"})
    List<Horario> findByProfessorId(Long professorId);

    @EntityGraph(attributePaths = {"professor", "disciplina", "turma"})
    List<Horario> findByTurmaId(Long turmaId);

    boolean existsByProfessorId(Long professorId);

    boolean existsByDisciplinaId(Long disciplinaId);

    boolean existsByTurmaId(Long turmaId);

    @Query("""
            select count(h) > 0 from Horario h
            where h.professor.id = :professorId
              and h.diaSemana = :diaSemana
              and h.horaInicio < :horaFim
              and h.horaFim > :horaInicio
              and h.id <> :ignorarId
            """)
    boolean existeConflitoProfessor(@Param("professorId") Long professorId,
                                    @Param("diaSemana") DiaSemana diaSemana,
                                    @Param("horaInicio") LocalTime horaInicio,
                                    @Param("horaFim") LocalTime horaFim,
                                    @Param("ignorarId") Long ignorarId);

    @Query("""
            select count(h) > 0 from Horario h
            where h.turma.id = :turmaId
              and h.diaSemana = :diaSemana
              and h.horaInicio < :horaFim
              and h.horaFim > :horaInicio
              and h.id <> :ignorarId
            """)
    boolean existeConflitoTurma(@Param("turmaId") Long turmaId,
                                @Param("diaSemana") DiaSemana diaSemana,
                                @Param("horaInicio") LocalTime horaInicio,
                                @Param("horaFim") LocalTime horaFim,
                                @Param("ignorarId") Long ignorarId);
}
