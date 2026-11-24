package br.edu.unisales.horariosapi.dto;

import br.edu.unisales.horariosapi.model.DiaSemana;
import br.edu.unisales.horariosapi.model.Horario;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalTime;

public record AulaResponse(
        Long horarioId,
        DiaSemana diaSemana,
        String diaSemanaDescricao,
        @JsonFormat(pattern = "HH:mm") @Schema(type = "string", example = "19:00") LocalTime horaInicio,
        @JsonFormat(pattern = "HH:mm") @Schema(type = "string", example = "20:40") LocalTime horaFim,
        String sala,
        String professor,
        String disciplina,
        String turma
) {

    public static AulaResponse de(Horario horario) {
        return new AulaResponse(
                horario.getId(),
                horario.getDiaSemana(),
                horario.getDiaSemana().getDescricao(),
                horario.getHoraInicio(),
                horario.getHoraFim(),
                horario.getSala(),
                horario.getProfessor().getNome(),
                horario.getDisciplina().getNome(),
                horario.getTurma().getNome());
    }
}
