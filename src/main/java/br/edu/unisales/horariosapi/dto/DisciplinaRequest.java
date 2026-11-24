package br.edu.unisales.horariosapi.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

@Schema(description = "Dados para cadastro ou atualização de disciplina")
public record DisciplinaRequest(

        @Schema(example = "ADS101")
        @NotBlank(message = "O código é obrigatório")
        @Size(max = 20, message = "O código deve ter no máximo 20 caracteres")
        String codigo,

        @Schema(example = "Programação Orientada a Objetos")
        @NotBlank(message = "O nome é obrigatório")
        @Size(max = 150, message = "O nome deve ter no máximo 150 caracteres")
        String nome,

        @Schema(example = "80")
        @NotNull(message = "A carga horária é obrigatória")
        @Positive(message = "A carga horária deve ser maior que zero")
        @Max(value = 400, message = "A carga horária deve ser de no máximo 400 horas")
        Integer cargaHoraria
) {
}
