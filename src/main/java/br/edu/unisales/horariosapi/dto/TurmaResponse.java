package br.edu.unisales.horariosapi.dto;

import br.edu.unisales.horariosapi.model.Turma;
import br.edu.unisales.horariosapi.model.Turno;

public record TurmaResponse(Long id, String nome, String curso, String anoLetivo, Turno turno) {

    public static TurmaResponse de(Turma turma) {
        return new TurmaResponse(
                turma.getId(),
                turma.getNome(),
                turma.getCurso(),
                turma.getAnoLetivo(),
                turma.getTurno());
    }
}
