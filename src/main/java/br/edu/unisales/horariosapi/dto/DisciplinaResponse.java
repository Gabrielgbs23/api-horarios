package br.edu.unisales.horariosapi.dto;

import br.edu.unisales.horariosapi.model.Disciplina;

public record DisciplinaResponse(Long id, String codigo, String nome, Integer cargaHoraria) {

    public static DisciplinaResponse de(Disciplina disciplina) {
        return new DisciplinaResponse(
                disciplina.getId(),
                disciplina.getCodigo(),
                disciplina.getNome(),
                disciplina.getCargaHoraria());
    }
}
