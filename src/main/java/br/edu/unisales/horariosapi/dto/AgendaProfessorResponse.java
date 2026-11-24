package br.edu.unisales.horariosapi.dto;

import java.util.List;

public record AgendaProfessorResponse(
        ProfessorResponse professor,
        List<DisciplinaResponse> disciplinas,
        List<TurmaResponse> turmas,
        List<AulaResponse> aulas
) {
}
