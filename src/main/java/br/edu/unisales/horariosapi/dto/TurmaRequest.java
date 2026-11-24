package br.edu.unisales.horariosapi.dto;

import br.edu.unisales.horariosapi.model.Turno;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

@Schema(description = "Dados para cadastro ou atualização de turma")
public record TurmaRequest(

        @Schema(example = "ADS-3A")
        @NotBlank(message = "O nome é obrigatório")
        @Size(max = 50, message = "O nome deve ter no máximo 50 caracteres")
        String nome,

        @Schema(example = "Análise e Desenvolvimento de Sistemas")
        @NotBlank(message = "O curso é obrigatório")
        @Size(max = 150, message = "O curso deve ter no máximo 150 caracteres")
        String curso,

        @Schema(example = "2026/2")
        @NotBlank(message = "O ano letivo é obrigatório")
        @Pattern(regexp = "\\d{4}(/[12])?", message = "O ano letivo deve estar no formato AAAA ou AAAA/S")
        String anoLetivo,

        @Schema(example = "NOTURNO")
        @NotNull(message = "O turno é obrigatório")
        Turno turno
) {
}
