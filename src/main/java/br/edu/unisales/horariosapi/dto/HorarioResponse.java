package br.edu.unisales.horariosapi.dto;

import br.edu.unisales.horariosapi.model.DiaSemana;
import br.edu.unisales.horariosapi.model.Horario;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalTime;

public record HorarioResponse(
        Long id,
        DiaSemana diaSemana,
        String diaSemanaDescricao,
        @JsonFormat(pattern = "HH:mm") @Schema(type = "string", example = "19:00") LocalTime horaInicio,
        @JsonFormat(pattern = "HH:mm") @Schema(type = "string", example = "20:40") LocalTime horaFim,
        String sala,
        ProfessorResponse professor,
        DisciplinaResponse disciplina,
        TurmaResponse turma
) {

    public static HorarioResponse de(Horario horario) {
        return new HorarioResponse(
                horario.getId(),
                horario.getDiaSemana(),
                horario.getDiaSemana().getDescricao(),
                horario.getHoraInicio(),
                horario.getHoraFim(),
                horario.getSala(),
                ProfessorResponse.de(horario.getProfessor()),
                DisciplinaResponse.de(horario.getDisciplina()),
                TurmaResponse.de(horario.getTurma()));
    }
}
