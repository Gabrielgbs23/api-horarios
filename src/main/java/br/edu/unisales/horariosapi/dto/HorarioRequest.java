package br.edu.unisales.horariosapi.dto;

import br.edu.unisales.horariosapi.model.DiaSemana;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.time.LocalTime;

@Schema(description = "Dados para cadastro ou atualização de horário de aula")
public record HorarioRequest(

        @Schema(example = "1")
        @NotNull(message = "O professor é obrigatório")
        @Positive(message = "O identificador do professor é inválido")
        Long professorId,

        @Schema(example = "1")
        @NotNull(message = "A disciplina é obrigatória")
        @Positive(message = "O identificador da disciplina é inválido")
        Long disciplinaId,

        @Schema(example = "1")
        @NotNull(message = "A turma é obrigatória")
        @Positive(message = "O identificador da turma é inválido")
        Long turmaId,

        @Schema(example = "SEGUNDA")
        @NotNull(message = "O dia da semana é obrigatório")
        DiaSemana diaSemana,

        @Schema(type = "string", example = "19:00")
        @NotNull(message = "A hora de início é obrigatória")
        LocalTime horaInicio,

        @Schema(type = "string", example = "20:40")
        @NotNull(message = "A hora de término é obrigatória")
        LocalTime horaFim,

        @Schema(example = "Sala 204")
        @Size(max = 30, message = "A sala deve ter no máximo 30 caracteres")
        String sala
) {
}
