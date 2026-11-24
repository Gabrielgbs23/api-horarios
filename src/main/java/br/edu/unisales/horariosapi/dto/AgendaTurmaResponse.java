package br.edu.unisales.horariosapi.dto;

import java.util.List;

public record AgendaTurmaResponse(
        TurmaResponse turma,
        List<ProfessorResponse> professores,
        List<DisciplinaResponse> disciplinas,
        List<AulaResponse> aulas
) {
}
